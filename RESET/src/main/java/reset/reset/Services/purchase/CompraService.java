package reset.reset.Services.purchase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.product.Produto;
import reset.reset.Models.product.ProdutoCompostoItem;
import reset.reset.Models.purchase.Compra;
import reset.reset.Models.purchase.CompraItem;
import reset.reset.Models.stock.Armazem;
import reset.reset.Models.stock.MovimentoStock;
import reset.reset.Models.stock.Stock;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.customer.FornecedorRepository;
import reset.reset.Repositories.product.IvaRepository;
import reset.reset.Repositories.product.ProdutoRepository;
import reset.reset.Repositories.purchase.CompraRepository;
import reset.reset.Repositories.stock.ArmazemRepository;
import reset.reset.Repositories.stock.MovimentoStockRepository;
import reset.reset.Repositories.stock.StockRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.BaseFilter;
import reset.reset.dto.purchase.CompraDTO;
import reset.reset.dto.purchase.CompraResumoDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CompraService extends BaseServiceImpl<Compra, Long, CompraRepository> {

    private final CompraRepository compraRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private IvaRepository ivaRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ArmazemRepository armazemRepository;
    @Autowired
    private MovimentoStockRepository movimentoStockRepository;

    public CompraService(CompraRepository repository) {
        super(repository);
        this.compraRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Compra compra) {
        validateEmpresaExists(compra.getEmpresa().getId());
        validateFornecedorExists(compra.getFornecedor().getId());
        validateCompraItens(compra.getItens());
        validateCompraTotal(compra.getTotal());
    }

    @Override
    protected void validateBeforeUpdate(Long id, Compra compra) {
        Compra existing = findByIdOrThrow(id);
        if ("CONFIRMADA".equals(existing.getEstado()) || "FINALIZADA".equals(existing.getEstado())) {
            throw new BusinessException("Cannot update a purchase that is already confirmed or finalized");
        }
        validateBeforeSave(compra);
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateFornecedorExists(Long fornecedorId) {
        if (!fornecedorRepository.existsById(fornecedorId)) {
            throw new EntityNotFoundException("Fornecedor not found with id: " + fornecedorId);
        }
    }

    private void validateCompraItens(List<CompraItem> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new BusinessException("Purchase must have at least one item");
        }

        for (CompraItem item : itens) {
            if (item.getQuantidade() == null || item.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Item quantity must be greater than zero");
            }
            if (item.getPrecoUnitario() == null || item.getPrecoUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Item price cannot be negative");
            }
            validateProdutoExists(item.getProduto().getId());
            if (item.getIva() != null) {
                validateIvaExists(item.getIva().getId());
            }
        }
    }

    private void validateProdutoExists(Long produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new EntityNotFoundException("Product not found with id: " + produtoId);
        }
    }

    private void validateIvaExists(Long ivaId) {
        if (!ivaRepository.existsById(ivaId)) {
            throw new EntityNotFoundException("IVA not found with id: " + ivaId);
        }
    }

    private void validateCompraTotal(BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Purchase total must be greater than zero");
        }
    }

    @Override
    @Transactional
    public Compra save(Compra compra) {
        if (compra.getTotal() == null) {
            BigDecimal total = calcularTotalCompra(compra);
            compra.setTotal(total);
        }

        if (compra.getData() == null) {
            compra.setData(LocalDate.now());
        }
        if (compra.getEstado() == null) {
            compra.setEstado("PENDENTE");
        }

        return super.save(compra);
    }

    private BigDecimal calcularTotalCompra(Compra compra) {
        if (compra.getItens() == null || compra.getItens().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return compra.getItens().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getPrecoUnitario().multiply(item.getQuantidade());
                    if (item.getIva() != null && item.getIva().getTaxa() != null) {
                        BigDecimal ivaValor = subtotal.multiply(
                                item.getIva().getTaxa().divide(new BigDecimal("100"))
                        );
                        subtotal = subtotal.add(ivaValor);
                    }
                    return subtotal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== MÉTODOS COM RETORNO DTO ====================

    public CompraDTO findByIdDTO(Long id) {
        Compra compra = findByIdOrThrow(id);
        return CompraDTO.fromEntity(compra);
    }

    public Page<CompraResumoDTO> filterSummary(BaseFilter filter) {
        Page<Compra> compras = compraRepository.findAll(filter.toPageable());
        return compras.map(CompraResumoDTO::fromEntity);
    }

    public Page<CompraResumoDTO> findByFornecedorIdSummary(Long fornecedorId, Pageable pageable) {
        Page<Compra> compras = compraRepository.findByFornecedorId(fornecedorId, pageable);
        return compras.map(CompraResumoDTO::fromEntity);
    }

    public Page<CompraResumoDTO> findByEstadoSummary(String estado, Pageable pageable) {
        Page<Compra> compras = compraRepository.findByEstadoPageable(estado, pageable);
        return compras.map(CompraResumoDTO::fromEntity);
    }

    public List<CompraDTO> findAllDTO() {
        return compraRepository.findAll().stream()
                .map(CompraDTO::fromEntity)
                .toList();
    }

    // ==================== MÉTODOS TRANSACIONAIS ====================

    @Transactional
    public Compra confirmarCompra(Long id) {
        Compra compra = findByIdOrThrow(id);

        if (!"PENDENTE".equals(compra.getEstado())) {
            throw new BusinessException("Only pending purchases can be confirmed");
        }

        compra.setEstado("CONFIRMADA");
        return compraRepository.save(compra);
    }

    @Transactional
    public Compra finalizarCompra(Long id, Long armazemId) {
        Compra compra = findByIdOrThrow(id);

        if (!"CONFIRMADA".equals(compra.getEstado()) && !"PENDENTE".equals(compra.getEstado())) {
            throw new BusinessException("Only confirmed or pending purchases can be finalized");
        }

        for (CompraItem item : compra.getItens()) {
            adicionarAoStock(item, armazemId, compra);
        }

        compra.setEstado("FINALIZADA");
        return compraRepository.save(compra);
    }

    @Transactional
    public void adicionarAoStock(CompraItem item, Long armazemId, Compra compra) {
        try {
            Long produtoId = item.getProduto().getId();
            BigDecimal quantidade = item.getQuantidade();
            Produto produto = item.getProduto();

            // Validar se o produto é composto
            if (produto.getIsComposto() != null && produto.getIsComposto()) {
                log.warn("Produto {} é composto. O stock será adicionado aos itens filhos.", produtoId);
                // Para produtos compostos, adicionar stock a cada item filho
                for (ProdutoCompostoItem compostoItem : produto.getItensComposto()) {
                    BigDecimal quantidadeFilho = compostoItem.getQuantidade().multiply(quantidade);
                    adicionarStockItem(compostoItem.getProdutoFilho(), armazemId, quantidadeFilho, compra, produto);
                }
                return;
            }

            // Produto simples - adicionar stock diretamente
            adicionarStockItem(produto, armazemId, quantidade, compra, null);

        } catch (Exception e) {
            log.error("Erro ao adicionar stock para item: {}", item.getId(), e);
            throw new BusinessException("Erro ao adicionar stock: " + e.getMessage());
        }
    }

    private void adicionarStockItem(Produto produto, Long armazemId, BigDecimal quantidade,
                                    Compra compra, Produto produtoPai) {
        try {
            Long produtoId = produto.getId();
            String nomeProduto = produto.getNome();

            // Validar armazém
            Armazem armazem = armazemRepository.findById(armazemId)
                    .orElseThrow(() -> new EntityNotFoundException("Armazém não encontrado com id: " + armazemId));

            // Buscar ou criar stock
            Stock stock = stockRepository.findByProdutoIdAndArmazemId(produtoId, armazemId)
                    .orElseGet(() -> {
                        Stock novoStock = new Stock();
                        novoStock.setProduto(produto);
                        novoStock.setArmazem(armazem);
                        novoStock.setQuantidadeAtual(BigDecimal.ZERO);
                        log.info("Criando novo registro de stock para produto {} no armazém {}", produtoId, armazemId);
                        return novoStock;
                    });

            // Atualizar quantidade
            BigDecimal quantidadeAnterior = stock.getQuantidadeAtual();
            BigDecimal novaQuantidade = quantidadeAnterior.add(quantidade);
            stock.setQuantidadeAtual(novaQuantidade);

            // Salvar stock
            stock = stockRepository.save(stock);

            // Determinar referência
            String referencia = "COMPRA-" + compra.getId();
            if (produtoPai != null) {
                referencia = referencia + "-" + produtoPai.getNome();
            }

            // Registrar movimento
            MovimentoStock movimento = new MovimentoStock();
            movimento.setEmpresa(compra.getEmpresa());
            movimento.setProduto(produto);
            movimento.setArmazem(armazem);
            movimento.setTipo("ENTRADA_COMPRA");
            movimento.setQuantidade(quantidade);
            movimento.setReferencia(referencia);
            movimento.setDataMovimento(LocalDateTime.now());
//            movimento.setObservacao(String.format(
//                    "Entrada de stock - Produto: %s, Quantidade: %s, Compra: #%d%s",
//                    nomeProduto, quantidade, compra.getId(),
//                    produtoPai != null ? " (componente de " + produtoPai.getNome() + ")" : ""
//            ));

            movimentoStockRepository.save(movimento);

            log.info("Stock adicionado - Produto: {}, Armazém: {}, Quantidade: {}, Anterior: {}, Nova: {}",
                    nomeProduto, armazemId, quantidade, quantidadeAnterior, novaQuantidade);

        } catch (Exception e) {
            log.error("Erro ao adicionar stock para produto: {}", produto.getId(), e);
            throw new BusinessException("Erro ao adicionar stock para " + produto.getNome() + ": " + e.getMessage());
        }
    }

    @Transactional
    public Compra cancelarCompra(Long id, String motivo) {
        Compra compra = findByIdOrThrow(id);

        if ("FINALIZADA".equals(compra.getEstado())) {
            throw new BusinessException("Cannot cancel a finalized purchase");
        }

        compra.setEstado("CANCELADA");
        return compraRepository.save(compra);
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    public Page<Compra> filter(BaseFilter filter) {
        return compraRepository.findAll(filter.toPageable());
    }

    public Page<Compra> findByFornecedorId(Long fornecedorId, Pageable pageable) {
        return compraRepository.findByFornecedorId(fornecedorId, pageable);
    }

    public List<Compra> findByEstado(String estado) {
        return compraRepository.findByEstado(estado);
    }

    public BigDecimal sumTotalByEmpresaAndPeriodo(Long empresaId, LocalDate inicio, LocalDate fim) {
        BigDecimal sum = compraRepository.sumTotalByEmpresaAndPeriodo(empresaId, inicio, fim);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}

