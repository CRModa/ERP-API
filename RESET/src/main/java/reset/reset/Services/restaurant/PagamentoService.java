package reset.reset.Services.restaurant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.User;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;
import reset.reset.Models.document.Documento;
import reset.reset.Models.document.DocumentoTipo;
import reset.reset.Models.document.DocumentoItem;
import reset.reset.Models.document.Tipos.Recibo;
import reset.reset.Models.financial.Conta;
import reset.reset.Models.financial.MovimentoConta;
import reset.reset.Models.product.Produto;
import reset.reset.Models.restaurant.ItemPedido;
import reset.reset.Models.restaurant.Pedido;
import reset.reset.Models.stock.Armazem;
import reset.reset.Models.stock.MovimentoStock;
import reset.reset.Models.stock.Stock;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.customer.ClienteRepository;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Repositories.document.DocumentoTipoRepository;
import reset.reset.Repositories.document.ReciboRepository;
import reset.reset.Repositories.financial.ContaRepository;
import reset.reset.Repositories.financial.MovimentoContaRepository;
import reset.reset.Repositories.product.ProdutoRepository;
import reset.reset.Repositories.restaurant.PedidoRepository;
import reset.reset.Repositories.stock.ArmazemRepository;
import reset.reset.Repositories.stock.MovimentoStockRepository;
import reset.reset.Repositories.stock.StockRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.stock.StockService;
import reset.reset.dto.restaurant.PagamentoRequest;
import reset.reset.dto.restaurant.PagamentoResponse;
import reset.reset.dto.restaurant.PagamentoEstatisticasDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagamentoService {

    private final PedidoRepository pedidoRepository;
    private final ContaRepository contaRepository;
    private final ReciboRepository documentoRepository;
    private final DocumentoTipoRepository documentoTipoRepository;
    private final MovimentoContaRepository movimentoContaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final ArmazemRepository armazemRepository;
    private final StockRepository stockRepository;
    private final MovimentoStockRepository movimentoStockRepository;

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

    /**
     * Processa o pagamento de um pedido, criando:
     * 1. Documento (Recibo/Fatura)
     * 2. Movimento na conta
     * 3. Atualiza o status do pedido
     * 4. Baixa o estoque
     */
    @Transactional
    public PagamentoResponse processarPagamento(PagamentoRequest request) {
        User currentUser = getAuthenticatedUser();
        Empresa empresa = currentUser.getEmpresa();

        // 1. Buscar pedido
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + request.getPedidoId()));

        if (pedido.getStatus() == Pedido.StatusPedido.FECHADO) {
            throw new BusinessException("Este pedido já foi pago");
        }

        // 2. Validar conta
        Conta conta = contaRepository.findById(request.getContaId())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada: " + request.getContaId()));

        if (!conta.getAtivo()) {
            throw new BusinessException("Conta inativa");
        }

        // 3. Validar cliente
        Cliente cliente = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + request.getClienteId()));
        } else if (pedido.getCliente() != null) {
            cliente = pedido.getCliente();
        }

        // 4. Buscar tipo de documento (Recibo)
        DocumentoTipo tipoRecibo = documentoTipoRepository.findByDescricao("RECIBO")
                .orElseGet(() -> {
                    // Criar tipo de documento se não existir
                    DocumentoTipo novoTipo = new DocumentoTipo();
                    novoTipo.setDescricao("RECIBO");
                    novoTipo.setClasse(DocumentoTipo.ClasseDocumento.VENDA);
                    novoTipo.setSeriePrefixo("REC");
                    novoTipo.setNumeracaoAutomatica(true);
                    novoTipo.setMovimentaStock(false);
                    novoTipo.setAfetaContas(true);
                    return documentoTipoRepository.save(novoTipo);
                });

        // 5. Criar documento (Recibo)
        Recibo documento = new Recibo();
        documento.setEmpresa(empresa);
        documento.setCliente(cliente);
        documento.setTipo(tipoRecibo);
        documento.setNumero(gerarNumeroDocumento(tipoRecibo, empresa.getId()));
        documento.setReferenciaPagamento(gerarNumeroDocumento(tipoRecibo, empresa.getId()));
        documento.setFormaPagamento(request.getMetodo());
        documento.setData(LocalDate.now());
        documento.setDataPagamento(LocalDateTime.now());
        documento.setTotal(pedido.getTotal());
        documento.setEstado("PAGO");
        documento.setObservacao("Pagamento do pedido #" + pedido.getNumero() + " - " + request.getObservacao());

        // Adicionar itens do pedido ao documento
        List<DocumentoItem> itensDocumento = pedido.getItens().stream()
                .map(itemPedido -> {
                    DocumentoItem item = new DocumentoItem();
                    item.setDocumento(documento);
                    item.setProduto(itemPedido.getProduto());
                    item.setQuantidade(itemPedido.getQuantidade());
                    item.setPrecoUnitario(itemPedido.getPrecoUnitario());
                    item.setIva(itemPedido.getIva());
                    item.setDesconto(itemPedido.getDesconto());
                    return item;
                })
                .collect(Collectors.toList());
        documento.setItens(itensDocumento);

        Recibo savedDocumento = documentoRepository.save(documento);

        // 6. Registrar movimento na conta
        MovimentoConta movimento = new MovimentoConta();
        movimento.setConta(conta);
        movimento.setDocumento(savedDocumento);
        movimento.setTipo("ENTRADA");
        movimento.setValor(pedido.getTotal());
        movimento.setData(LocalDate.now());
        movimento.setObservacao("Pagamento do pedido #" + pedido.getNumero() + " - " + request.getMetodo());
        movimentoContaRepository.save(movimento);

        // 7. Atualizar status do pedido
        pedido.setStatus(Pedido.StatusPedido.FECHADO);
        pedido.setDataFechamento(LocalDateTime.now());
        pedidoRepository.save(pedido);

        // 8. Baixar estoque (para produtos simples)
        baixarEstoquePedido(pedido);

        // 9. Atualizar saldo do cliente (se houver)
        if (cliente != null) {
            BigDecimal novoSaldo = cliente.getSaldoCorrente().subtract(pedido.getTotal());
            cliente.setSaldoCorrente(novoSaldo);
            clienteRepository.save(cliente);
        }

        // 10. Registrar log
        log.info("Pagamento processado - Pedido: {}, Documento: {}, Valor: {}, Método: {}",
                pedido.getNumero(), savedDocumento.getNumero(), pedido.getTotal(), request.getMetodo());

        return PagamentoResponse.builder()
                .id(savedDocumento.getId())
                .numeroDocumento(savedDocumento.getNumero())
                .pedidoId(pedido.getId())
                .pedidoNumero(pedido.getNumero())
                .contaId(conta.getId())
                .contaNome(conta.getDescricao())
                .clienteId(cliente != null ? cliente.getId() : null)
                .clienteNome(cliente != null ? cliente.getNome() : null)
                .valor(pedido.getTotal())
                .metodo(request.getMetodo())
                .troco(request.getTroco() != null ? request.getTroco() : BigDecimal.ZERO)
                .status("CONFIRMADO")
                .dataPagamento(LocalDateTime.now())
                .observacao(request.getObservacao())
                .tipoDocumento("RECIBO")
                .build();
    }

    @Transactional
    public List<PagamentoResponse> processarPagamentosMultiplos(List<PagamentoRequest> requests) {
        return requests.stream()
                .map(this::processarPagamento)
                .collect(Collectors.toList());
    }

//    private void baixarEstoquePedido(Pedido pedido) {
//        // Buscar armazém padrão
//        Armazem armazem = armazemRepository.findFirstByEmpresa(getAuthenticatedUser().getEmpresa()).orElseThrow(null);
//
//        for (ItemPedido item : pedido.getItens()) {
//            Produto produto = item.getProduto();
//
//            // Verificar se é produto composto
//            if (produto.getIsComposto()) {
//                for (var composto : produto.getItensComposto()) {
//                    BigDecimal quantidade = composto.getQuantidade().multiply(item.getQuantidade());
//                    stockService.removerStock(
//                            composto.getProdutoFilho().getId(),
//                            armazem.getId(),
//                            quantidade,
//                            "Pedido #" + pedido.getNumero()
//                    );
//                }
//            } else {
//                stockService.removerStock(
//                        produto.getId(),
//                        armazem.getId(),
//                        item.getQuantidade(),
//                        "Pedido #" + pedido.getNumero()
//                );
//            }
//        }
//    }

    @Transactional
    public void baixarEstoquePedido(Pedido pedido) {
        log.info("Iniciando baixa de estoque em lote para pedido: {}", pedido.getNumero());

        // 1. Coletar todos os produtos e quantidades necessárias
        Map<Long, BigDecimal> necessidadesPorProduto = new HashMap<>();

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();

            if (produto.getIsComposto()) {
                for (var composto : produto.getItensComposto()) {
                    BigDecimal quantidade = composto.getQuantidade().multiply(item.getQuantidade());
                    necessidadesPorProduto.merge(
                            composto.getProdutoFilho().getId(),
                            quantidade,
                            BigDecimal::add
                    );
                }
            } else {
                necessidadesPorProduto.merge(
                        produto.getId(),
                        item.getQuantidade(),
                        BigDecimal::add
                );
            }
        }

        // 2. Buscar todos os stocks necessários em uma única consulta
        List<Long> produtoIds = new ArrayList<>(necessidadesPorProduto.keySet());
        List<Stock> todosStocks = stockRepository.findByProdutoIds(produtoIds);

        // 3. Agrupar stocks por produto
        Map<Long, List<Stock>> stocksPorProduto = todosStocks.stream()
                .filter(s -> s.getQuantidadeAtual().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.groupingBy(s -> s.getProduto().getId()));

        // 4. Processar cada produto
        List<Stock> stocksParaAtualizar = new ArrayList<>();
        List<MovimentoStock> movimentos = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : necessidadesPorProduto.entrySet()) {
            Long produtoId = entry.getKey();
            BigDecimal quantidadeNecessaria = entry.getValue();

            List<Stock> stocksProduto = stocksPorProduto.getOrDefault(produtoId, new ArrayList<>());

            if (stocksProduto.isEmpty()) {
                Produto produto = produtoRepository.findById(produtoId)
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + produtoId));
                throw new BusinessException("Stock insuficiente para o produto: " + produto.getNome());
            }

            // Ordenar por quantidade (maior primeiro)
            stocksProduto.sort((s1, s2) -> s2.getQuantidadeAtual().compareTo(s1.getQuantidadeAtual()));

            BigDecimal quantidadeRestante = quantidadeNecessaria;

            for (Stock stock : stocksProduto) {
                if (quantidadeRestante.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                BigDecimal quantidadeDisponivel = stock.getQuantidadeAtual();
                BigDecimal quantidadeBaixar = quantidadeRestante.min(quantidadeDisponivel);

                // Atualizar stock
                stock.setQuantidadeAtual(quantidadeDisponivel.subtract(quantidadeBaixar));
                stocksParaAtualizar.add(stock);

                // Criar movimento
                MovimentoStock movimento = new MovimentoStock();
                movimento.setEmpresa(stock.getProduto().getEmpresa());
                movimento.setProduto(stock.getProduto());
                movimento.setArmazem(stock.getArmazem());
                movimento.setTipo("SAIDA_VENDA");
                movimento.setQuantidade(quantidadeBaixar);
                movimento.setReferencia("Pedido #" + pedido.getNumero());
                movimento.setDataMovimento(LocalDateTime.now());
                movimento.setObservacao("Baixa de estoque em lote - Pedido #" + pedido.getNumero());
                movimentos.add(movimento);

                quantidadeRestante = quantidadeRestante.subtract(quantidadeBaixar);
            }
        }

        // 5. Salvar tudo em lote
        if (!stocksParaAtualizar.isEmpty()) {
            stockRepository.saveAll(stocksParaAtualizar);
        }

        if (!movimentos.isEmpty()) {
            movimentoStockRepository.saveAll(movimentos);
        }

        log.info("Baixa de estoque em lote concluída. {} stocks atualizados, {} movimentos criados.",
                stocksParaAtualizar.size(), movimentos.size());
    }

    private String gerarNumeroDocumento(DocumentoTipo tipo, Long empresaId) {
        String prefixo = tipo.getSeriePrefixo() != null ? tipo.getSeriePrefixo() : "DOC";
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = documentoRepository.countByEmpresaIdAndTipoId(empresaId, tipo.getId()) + 1;
        return prefixo + data + String.format("%04d", count);
    }

    public List<PagamentoResponse> findPagamentosByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Long empresaId = getCurrentEmpresaId();
        List<Recibo> documentos = documentoRepository.findByEmpresaIdAndDataBetween(
                empresaId, inicio.toLocalDate(), fim.toLocalDate()
        );

        return documentos.stream()
                .filter(doc -> "RECIBO".equals(doc.getTipo().getDescricao()))
                .map(doc -> PagamentoResponse.builder()
                        .id(doc.getId())
                        .numeroDocumento(doc.getNumero())
                        .pedidoId(null) // Não temos relação direta
                        .valor(doc.getTotal())
                        .dataPagamento(doc.getData().atStartOfDay())
                        .clienteId(doc.getCliente() != null ? doc.getCliente().getId() : null)
                        .clienteNome(doc.getCliente() != null ? doc.getCliente().getNome() : null)
                        .status(doc.getEstado())
                        .tipoDocumento("RECIBO")
                        .build())
                .collect(Collectors.toList());
    }

    public PagamentoEstatisticasDTO getEstatisticasPagamentos() {
        Long empresaId = getCurrentEmpresaId();
        LocalDateTime hojeInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hojeFim = LocalDateTime.now();

        // Total de pagamentos
        Long totalPagamentos = documentoRepository.countByEmpresaIdAndTipoDescricao(empresaId, "RECIBO");

        // Valor total
        BigDecimal valorTotal = documentoRepository.sumTotalByEmpresaIdAndTipoDescricao(empresaId, "RECIBO");

        // Valor de hoje
        BigDecimal valorTotalHoje = documentoRepository.sumTotalByEmpresaIdAndTipoDescricaoAndPeriodo(
                empresaId, "RECIBO", hojeInicio, hojeFim
        );

        // Pagamentos por metodo (via movimentos de conta)
        List<Object[]> metodoStats = movimentoContaRepository.countAndSumByMetodo(empresaId);
        Map<String, Long> pagamentosPorMetodo = new HashMap<>();
        Map<String, BigDecimal> valorPorMetodo = new HashMap<>();

        for (Object[] row : metodoStats) {
            String metodo = (String) row[0];
            Long count = (Long) row[1];
            BigDecimal sum = (BigDecimal) row[2];
            pagamentosPorMetodo.put(metodo, count);
            valorPorMetodo.put(metodo, sum);
        }

        // Total de pedidos pagos (fechados)
        Long totalPedidosPagos = pedidoRepository.countByEmpresaIdAndStatus(empresaId, Pedido.StatusPedido.FECHADO);

        return PagamentoEstatisticasDTO.builder()
                .totalPagamentos(totalPagamentos != null ? totalPagamentos : 0L)
                .valorTotal(valorTotal != null ? valorTotal : BigDecimal.ZERO)
                .valorTotalHoje(valorTotalHoje != null ? valorTotalHoje : BigDecimal.ZERO)
                .pagamentosPorMetodo(pagamentosPorMetodo)
                .valorPorMetodo(valorPorMetodo)
                .totalPedidosPagos(totalPedidosPagos != null ? totalPedidosPagos : 0L)
                .build();
    }

    @Transactional
    public void cancelarPagamento(Long documentoId, String motivo) {
        Recibo documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

        if (!"RECIBO".equals(documento.getTipo().getDescricao())) {
            throw new BusinessException("Documento não é um recibo de pagamento");
        }

        if ("CANCELADO".equals(documento.getEstado())) {
            throw new BusinessException("Pagamento já foi cancelado");
        }

        // Cancelar documento
        documento.setEstado("CANCELADO");
        documento.setObservacao((documento.getObservacao() != null ? documento.getObservacao() + " " : "") +
                "Cancelado: " + motivo);
        documentoRepository.save(documento);

        // Reverter movimento na conta
        // Buscar movimento relacionado ao documento
        List<MovimentoConta> movimentos = movimentoContaRepository.findByDocumentoId(documento.getId());
        for (MovimentoConta movimento : movimentos) {
            MovimentoConta reversao = new MovimentoConta();
            reversao.setConta(movimento.getConta());
            reversao.setDocumento(documento);
            reversao.setTipo("SAIDA");
            reversao.setValor(movimento.getValor());
            reversao.setData(LocalDate.now());
            reversao.setObservacao("Estorno: " + motivo + " - Documento " + documento.getNumero());
            movimentoContaRepository.save(reversao);
        }

        // Restaurar status do pedido (se houver referência)
        // Em produção, seria necessário guardar referência do pedido no documento
        log.info("Pagamento cancelado - Documento: {}, Motivo: {}", documento.getNumero(), motivo);
    }
}