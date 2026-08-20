package reset.reset.Services.restaurant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.Services.stock.StockService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PedidoService extends BaseServiceImpl<Pedido, Long, PedidoRepository> {

    private final PedidoRepository pedidoRepository;

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ComboRepository comboRepository;

    @Autowired
    private UserRepository utilizadorRepository;

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

    @Override
    protected void validateBeforeSave(Pedido pedido) {
        validateEmpresaExists(pedido.getEmpresa().getId());
        validateMesa(pedido.getMesa());
        validateCliente(pedido.getCliente());
        validateAtendente(pedido.getAtendente());
        validateGarcom(pedido.getGarcom());
        validateItensPedido(pedido.getItens());
        validateTipoPedido(pedido.getTipo());
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa não encontrada");
        }
    }

    private void validateMesa(Mesa mesa) {
        if (mesa != null && mesa.getId() != null) {
            Mesa mesaExistente = mesaRepository.findById(mesa.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Mesa não encontrada"));

            if (mesaExistente.getStatus() == Mesa.StatusMesa.OCUPADA) {
                throw new BusinessException("Mesa já está ocupada");
            }
        }
    }

    private void validateCliente(Cliente cliente) {
        if (cliente != null && cliente.getId() != null) {
            if (!clienteRepository.existsById(cliente.getId())) {
                throw new EntityNotFoundException("Cliente não encontrado");
            }
        }
    }

    private void validateAtendente(User atendente) {
        if (atendente != null && atendente.getId() != null) {
            if (!utilizadorRepository.existsById(atendente.getId())) {
                throw new EntityNotFoundException("Atendente não encontrado");
            }
        }
    }

    private void validateGarcom(User garcom) {
        if (garcom != null && garcom.getId() != null) {
            if (!utilizadorRepository.existsById(garcom.getId())) {
                throw new EntityNotFoundException("Garçom não encontrado");
            }
        }
    }

    private void validateItensPedido(List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new BusinessException("Pedido deve ter pelo menos um item");
        }

        for (ItemPedido item : itens) {
            if (item.getQuantidade() == null || item.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Quantidade deve ser maior que zero");
            }

            if (item.getProduto() != null && item.getProduto().getId() != null) {
                Produto produto = produtoRepository.findById(item.getProduto().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

                if (!produto.getDisponivel()) {
                    throw new BusinessException("Produto " + produto.getNome() + " não está disponível");
                }

                item.setPrecoUnitario(produto.getPrecoVenda());

                // Verificar stock para produtos não compostos
                if (!produto.getIsComposto()) {
                    BigDecimal stockDisponivel = stockService.getQuantidadeTotalPorProduto(produto.getId());
                    if (stockDisponivel.compareTo(item.getQuantidade()) < 0) {
                        throw new BusinessException("Stock insuficiente para " + produto.getNome() +
                                " (disponível: " + stockDisponivel + ")");
                    }
                }
            }

            if (item.getCombo() != null && item.getCombo().getId() != null) {
                if (!comboRepository.existsById(item.getCombo().getId())) {
                    throw new EntityNotFoundException("Combo não encontrado");
                }
            }

            validateTipoPedidoItem(item);
        }
    }

    private void validateTipoPedido(Pedido.TipoPedido tipo) {
        if (tipo == null) {
            throw new BusinessException("Tipo de pedido é obrigatório");
        }
    }

    private void validateTipoPedidoItem(ItemPedido item) {
        if (item.getProduto() == null && item.getCombo() == null) {
            throw new BusinessException("Item deve ter um produto ou um combo");
        }
    }

    @Override
    @Transactional
    public Pedido save(Pedido pedido) {
        // Gerar número do pedido
        if (pedido.getNumero() == null) {
            pedido.setNumero(gerarNumeroPedido(pedido.getEmpresa().getId()));
        }

        // Definir data do pedido
        if (pedido.getDataPedido() == null) {
            pedido.setDataPedido(LocalDateTime.now());
        }

        // Calcular totais
        calcularTotaisPedido(pedido);

        Pedido saved = super.save(pedido);

        // Atualizar status da mesa
        if (saved.getMesa() != null && saved.getMesa().getId() != null) {
            Mesa mesa = mesaRepository.findById(saved.getMesa().getId()).orElse(null);
            if (mesa != null && mesa.getStatus() == Mesa.StatusMesa.DISPONIVEL) {
                mesa.setStatus(Mesa.StatusMesa.OCUPADA);
                mesaRepository.save(mesa);
            }
        }

        // Registrar log
        registrarLog(saved, "CRIACAO", null, "Pedido criado");

        return saved;
    }

    private String gerarNumeroPedido(Long empresaId) {
        String prefixo = "PED";
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = pedidoRepository.countByEmpresaAndPeriodo(empresaId,
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now()) + 1;
        return prefixo + data + String.format("%04d", count);
    }

    private void calcularTotaisPedido(Pedido pedido) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemPedido item : pedido.getItens()) {
            BigDecimal preco = item.getPrecoUnitario() != null ? item.getPrecoUnitario() : BigDecimal.ZERO;
            BigDecimal quantidade = item.getQuantidade();
            BigDecimal subtotalItem = preco.multiply(quantidade);

            // Aplicar desconto se houver
            if (item.getDescontoValor() != null) {
                subtotalItem = subtotalItem.subtract(item.getDescontoValor());
            }

            item.setSubtotal(subtotalItem);
            subtotal = subtotal.add(subtotalItem);
        }

        pedido.setSubtotal(subtotal);

        // Aplicar desconto geral
        BigDecimal desconto = pedido.getDesconto() != null ? pedido.getDesconto() : BigDecimal.ZERO;
        BigDecimal totalComDesconto = subtotal.subtract(desconto);

        // Aplicar taxa de serviço (10% padrão)
        BigDecimal taxaServico = pedido.getTaxaServico() != null ? pedido.getTaxaServico() : totalComDesconto.multiply(new BigDecimal("0.10"));
        pedido.setTaxaServico(taxaServico);

        BigDecimal total = totalComDesconto.add(taxaServico);
        pedido.setTotal(total);
    }

    @Transactional
    public Pedido atualizarStatus(Long pedidoId, Pedido.StatusPedido novoStatus) {
        Pedido pedido = findByIdOrThrow(pedidoId);

        String statusAnterior = pedido.getStatus().toString();
        pedido.setStatus(novoStatus);

        if (novoStatus == Pedido.StatusPedido.PRONTO) {
            pedido.setDataEntrega(LocalDateTime.now());

            // Calcular tempo de espera
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
                if (mesa != null) {
                    mesa.setStatus(Mesa.StatusMesa.DISPONIVEL);
                    mesaRepository.save(mesa);
                }
            }
        }

        Pedido updated = pedidoRepository.save(pedido);
        registrarLog(updated, "MUDANCA_STATUS", statusAnterior, "Status alterado para: " + novoStatus);

        return updated;
    }

    @Transactional
    public Pedido adicionarItem(Long pedidoId, ItemPedido novoItem) {
        Pedido pedido = findByIdOrThrow(pedidoId);

        if (pedido.getStatus() == Pedido.StatusPedido.FECHADO ||
                pedido.getStatus() == Pedido.StatusPedido.CANCELADO) {
            throw new BusinessException("Não é possível adicionar itens a um pedido finalizado ou cancelado");
        }

        // Validar item
        if (novoItem.getProduto() != null && novoItem.getProduto().getId() != null) {
            Produto produto = produtoRepository.findById(novoItem.getProduto().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
            novoItem.setPrecoUnitario(produto.getPrecoVenda());
        }

        novoItem.setPedido(pedido);
        novoItem.setStatus(ItemPedido.StatusItemPedido.PENDENTE);

        // Calcular subtotal do item
        BigDecimal preco = novoItem.getPrecoUnitario() != null ? novoItem.getPrecoUnitario() : BigDecimal.ZERO;
        BigDecimal quantidade = novoItem.getQuantidade();
        BigDecimal subtotalItem = preco.multiply(quantidade);

        if (novoItem.getDescontoValor() != null) {
            subtotalItem = subtotalItem.subtract(novoItem.getDescontoValor());
        }
        novoItem.setSubtotal(subtotalItem);

        pedido.getItens().add(novoItem);

        // Recalcular totais
        calcularTotaisPedido(pedido);

        Pedido updated = pedidoRepository.save(pedido);
        registrarLog(updated, "ADICAO_ITEM", null, "Item adicionado: " + novoItem.getProduto().getNome());

        return updated;
    }

    @Transactional
    public Pedido removerItem(Long pedidoId, Long itemId) {
        Pedido pedido = findByIdOrThrow(pedidoId);

        if (pedido.getStatus() == Pedido.StatusPedido.FECHADO ||
                pedido.getStatus() == Pedido.StatusPedido.CANCELADO) {
            throw new BusinessException("Não é possível remover itens de um pedido finalizado ou cancelado");
        }

        pedido.getItens().removeIf(item -> item.getId().equals(itemId));

        // Recalcular totais
        calcularTotaisPedido(pedido);

        Pedido updated = pedidoRepository.save(pedido);
        registrarLog(updated, "REMOCAO_ITEM", null, "Item removido ID: " + itemId);

        return updated;
    }

    private void registrarLog(Pedido pedido, String acao, String statusAnterior, String descricao) {
        PedidoLog log = new PedidoLog();
        log.setPedido(pedido);
        log.setAcao(acao);
        log.setStatusAnterior(statusAnterior);
        log.setStatusNovo(pedido.getStatus().toString());
        log.setDescricao(descricao);
        pedidoLogRepository.save(log);
    }

    public List<Pedido> findPedidosEmAndamento(Long empresaId) {
        return pedidoRepository.findPedidosEmAndamento(empresaId);
    }

    public List<Pedido> findPedidosAtivosByMesa(Long mesaId) {
        return pedidoRepository.findPedidosAtivosByMesaId(mesaId);
    }

    public Page<Pedido> findByEmpresaId(Long empresaId, Pageable pageable) {
        return pedidoRepository.findByEmpresaId(empresaId, pageable);
    }

    public BigDecimal sumTotalByEmpresaAndPeriodo(Long empresaId, LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal sum = pedidoRepository.sumTotalByEmpresaAndPeriodo(empresaId, inicio, fim);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public Long countByEmpresaAndPeriodo(Long empresaId, LocalDateTime inicio, LocalDateTime fim) {
        return pedidoRepository.countByEmpresaAndPeriodo(empresaId, inicio, fim);
    }
}