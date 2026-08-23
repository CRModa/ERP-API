package reset.reset.Services.restaurant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.User;
import reset.reset.Models.customer.Cliente;
import reset.reset.Models.product.Produto;
import reset.reset.Models.restaurant.*;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.customer.ClienteRepository;
import reset.reset.Repositories.product.ProdutoRepository;
import reset.reset.Repositories.restaurant.*;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.Services.stock.StockService;
import reset.reset.dto.request.restaurant.ItemPedidoRequest;
import reset.reset.dto.request.restaurant.PedidoRequest;
import reset.reset.dto.restaurant.ItemPedidoDTO;
import reset.reset.dto.restaurant.PedidoDTO;
import reset.reset.dto.restaurant.PedidoResumoDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PedidoService extends BaseServiceImpl<Pedido, Long, PedidoRepository> {

    private final PedidoRepository pedidoRepository;

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PedidoLogRepository pedidoLogRepository;

    @Autowired
    private StockService stockService;

    public PedidoService(PedidoRepository repository) {
        super(repository);
        this.pedidoRepository = repository;
    }

    private User getAuthenticatedUser() {
        try {
            UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userRepository.findById(principal.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        } catch (Exception e) {
            throw new BusinessException("Usuário não autenticado");
        }
    }

    private Long getCurrentEmpresaId() {
        return getAuthenticatedUser().getEmpresa().getId();
    }

    // ==================== CRUD COM DTOs ====================

    @Transactional
    public PedidoDTO criarPedido(PedidoRequest request) {
        User currentUser = getAuthenticatedUser();

        // Validar itens
        if (request.getItens() == null || request.getItens().isEmpty()) {
            throw new BusinessException("Pedido deve ter pelo menos um item");
        }

        // Criar o pedido
        Pedido pedido = new Pedido();
        pedido.setEmpresa(currentUser.getEmpresa());
        pedido.setAtendente(currentUser);
        pedido.setGarcom(currentUser);
        pedido.setTipo(Pedido.TipoPedido.valueOf(request.getTipo()));
        pedido.setObservacao(request.getObservacao());
        pedido.setStatus(Pedido.StatusPedido.PENDENTE);
        pedido.setNumero(gerarNumeroPedido(getCurrentEmpresaId()));
        pedido.setDataPedido(LocalDateTime.now());

        // Buscar mesa se informada
        if (request.getMesaId() != null) {
            Mesa mesa = mesaRepository.findById(request.getMesaId())
                    .orElseThrow(() -> new EntityNotFoundException("Mesa não encontrada: " + request.getMesaId()));

            if (mesa.getStatus() == Mesa.StatusMesa.DISPONIVEL) {
                mesa.setStatus(Mesa.StatusMesa.OCUPADA);
                mesaRepository.save(mesa);
            }
            pedido.setMesa(mesa);
        }

        // Buscar cliente se informado
        if (request.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            pedido.setCliente(cliente);
        }

        // Adicionar itens
        List<ItemPedido> itens = new ArrayList<>();
        for (ItemPedidoRequest itemRequest : request.getItens()) {
            Produto produto = produtoRepository.findById(itemRequest.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemRequest.getProdutoId()));

            if (!produto.getDisponivel()) {
                throw new BusinessException("Produto " + produto.getNome() + " não está disponível");
            }

            // Verificar estoque
            if (!produto.getIsComposto()) {
                BigDecimal stockDisponivel = stockService.getQuantidadeTotalPorProduto(produto.getId());
                if (stockDisponivel.compareTo(itemRequest.getQuantidade()) < 0) {
                    throw new BusinessException("Stock insuficiente para " + produto.getNome() +
                            " (disponível: " + stockDisponivel + ")");
                }
            }

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(itemRequest.getQuantidade() != null ? itemRequest.getQuantidade() : BigDecimal.ONE);
            item.setPrecoUnitario(
                    itemRequest.getPrecoUnitario() != null ? itemRequest.getPrecoUnitario() : produto.getPrecoVenda()
            );
            item.setObservacao(itemRequest.getObservacao());
            item.setStatus(ItemPedido.StatusItemPedido.PENDENTE);
            item.calcularSubtotal();
            itens.add(item);
        }

        pedido.setItens(itens);
        pedido.recalcularTotais();

        Pedido saved = super.save(pedido);

        // Registrar log
        registrarLog(saved, "CRIACAO", null, "Pedido criado com " + saved.getItens().size() + " itens");

        return PedidoDTO.fromEntity(saved);
    }

    @Transactional
    public PedidoDTO atualizarStatus(Long pedidoId, Pedido.StatusPedido novoStatus) {
        Pedido pedido = findByIdOrThrow(pedidoId);

        String statusAnterior = pedido.getStatus().toString();
        pedido.setStatus(novoStatus);

        if (novoStatus == Pedido.StatusPedido.PRONTO) {
            pedido.setDataEntrega(LocalDateTime.now());
            if (pedido.getDataPedido() != null) {
                long minutos = java.time.Duration.between(pedido.getDataPedido(), LocalDateTime.now()).toMinutes();
                pedido.setTempoEspera((int) minutos);
            }
        }

        if (novoStatus == Pedido.StatusPedido.FECHADO || novoStatus == Pedido.StatusPedido.ENTREGUE) {
            pedido.setDataFechamento(LocalDateTime.now());
            // Liberar mesa
            if (pedido.getMesa() != null) {
                Mesa mesa = mesaRepository.findById(pedido.getMesa().getId()).orElse(null);
                if (mesa != null && mesa.getStatus() == Mesa.StatusMesa.OCUPADA) {
                    List<Pedido> pedidosAtivos = pedidoRepository.findPedidosAtivosByMesaId(mesa.getId());
                    boolean hasActiveOrders = pedidosAtivos.stream()
                            .anyMatch(p -> !p.getId().equals(pedidoId) &&
                                    (p.getStatus() == Pedido.StatusPedido.PENDENTE ||
                                            p.getStatus() == Pedido.StatusPedido.EM_PREPARO ||
                                            p.getStatus() == Pedido.StatusPedido.PRONTO));

                    if (!hasActiveOrders) {
                        mesa.setStatus(Mesa.StatusMesa.DISPONIVEL);
                        mesaRepository.save(mesa);
                    }
                }
            }
        }

        Pedido updated = pedidoRepository.save(pedido);
        registrarLog(updated, "MUDANCA_STATUS", statusAnterior, "Status alterado para: " + novoStatus);

        return PedidoDTO.fromEntity(updated);
    }

    @Transactional
    public PedidoDTO adicionarItem(Long pedidoId, ItemPedidoRequest request) {
        Pedido pedido = findByIdOrThrow(pedidoId);

        if (!pedido.isAceitaItens()) {
            throw new BusinessException("Não é possível adicionar itens a um pedido " + pedido.getStatus());
        }

        Produto produto = produtoRepository.findById(request.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        if (!produto.getDisponivel()) {
            throw new BusinessException("Produto " + produto.getNome() + " não está disponível");
        }

        if (!produto.getIsComposto()) {
            BigDecimal stockDisponivel = stockService.getQuantidadeTotalPorProduto(produto.getId());
            if (stockDisponivel.compareTo(request.getQuantidade()) < 0) {
                throw new BusinessException("Stock insuficiente para " + produto.getNome() +
                        " (disponível: " + stockDisponivel + ")");
            }
        }

        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(request.getQuantidade() != null ? request.getQuantidade() : BigDecimal.ONE);
        item.setPrecoUnitario(
                request.getPrecoUnitario() != null ? request.getPrecoUnitario() : produto.getPrecoVenda()
        );
        item.setObservacao(request.getObservacao());
        item.setStatus(ItemPedido.StatusItemPedido.PENDENTE);
        item.calcularSubtotal();

        pedido.adicionarItem(item);

        Pedido updated = pedidoRepository.save(pedido);
        registrarLog(updated, "ADICAO_ITEM", null, "Item adicionado: " + produto.getNome());

        return PedidoDTO.fromEntity(updated);
    }

    @Transactional
    public PedidoDTO removerItem(Long pedidoId, Long itemId) {
        Pedido pedido = findByIdOrThrow(pedidoId);

        if (!pedido.isAceitaItens()) {
            throw new BusinessException("Não é possível remover itens de um pedido " + pedido.getStatus());
        }

        ItemPedido item = pedido.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado"));

        String nomeProduto = item.getProduto() != null ? item.getProduto().getNome() : "Item desconhecido";

        pedido.removerItem(itemId);
        Pedido updated = pedidoRepository.save(pedido);
        registrarLog(updated, "REMOCAO_ITEM", null, "Item removido: " + nomeProduto);

        return PedidoDTO.fromEntity(updated);
    }

    // ==================== CONSULTAS COM DTOs ====================

    public PedidoDTO findPedidoDTOById(Long id) {
        Pedido pedido = findByIdOrThrow(id);
        return PedidoDTO.fromEntity(pedido);
    }

    public Page<PedidoResumoDTO> findPedidosResumoByEmpresa(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        Page<Pedido> pedidos = pedidoRepository.findByEmpresaId(empresaId, pageable);
        return pedidos.map(PedidoResumoDTO::fromEntity);
    }

    public List<PedidoDTO> findPedidosAtivosByMesaDTO(Long mesaId) {
        List<Pedido> pedidos = pedidoRepository.findPedidosAtivosByMesaId(mesaId);
        return pedidos.stream()
                .map(PedidoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PedidoDTO> findPedidosEmAndamento() {
        Long empresaId = getCurrentEmpresaId();
        List<Pedido> pedidos = pedidoRepository.findPedidosEmAndamento(empresaId);
        return pedidos.stream()
                .map(PedidoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== UTILITÁRIOS ====================

    private String gerarNumeroPedido(Long empresaId) {
        String prefixo = "PED";
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = pedidoRepository.countByEmpresaAndPeriodo(empresaId,
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now()) + 1;
        return prefixo + data + String.format("%04d", count);
    }

    private void registrarLog(Pedido pedido, String acao, String statusAnterior, String descricao) {
        PedidoLog log = new PedidoLog();
        log.setPedido(pedido);
        log.setAcao(acao);
        log.setStatusAnterior(statusAnterior);
        log.setStatusNovo(pedido.getStatus().toString());
        log.setDescricao(descricao);
        log.setUtilizador(getAuthenticatedUser());
        pedidoLogRepository.save(log);
    }

    // Métodos legado (compatibilidade)
    public Pedido findPedidoById(Long id) {
        return findByIdOrThrow(id);
    }

    public List<Pedido> findPedidosAtivosByMesa(Long mesaId) {
        return pedidoRepository.findPedidosAtivosByMesaId(mesaId);
    }

    public Page<Pedido> findByEmpresaId(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        return pedidoRepository.findByEmpresaId(empresaId, pageable);
    }
}