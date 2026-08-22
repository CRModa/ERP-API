package reset.reset.Services.stock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Exceptions.InsufficientStockException;
import reset.reset.Models.product.Produto;
import reset.reset.Models.product.ProdutoCompostoItem;
import reset.reset.Models.stock.Armazem;
import reset.reset.Models.stock.MovimentoStock;
import reset.reset.Models.stock.Stock;
import reset.reset.Repositories.product.ProdutoRepository;
import reset.reset.Repositories.stock.ArmazemRepository;
import reset.reset.Repositories.stock.MovimentoStockRepository;
import reset.reset.Repositories.stock.StockRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.StockFilter;
import reset.reset.dto.stock.StockDTO;
import reset.reset.dto.stock.StockResumoDTO;
import reset.reset.dto.request.restaurant.PedidoItemRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockService extends BaseServiceImpl<Stock, Long, StockRepository> {

    private final StockRepository stockRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ArmazemRepository armazemRepository;

    @Autowired
    private MovimentoStockRepository movimentoStockRepository;

    public StockService(StockRepository repository) {
        super(repository);
        this.stockRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Stock stock) {
        validateProdutoExists(stock.getProduto().getId());
        validateArmazemExists(stock.getArmazem().getId());
        validateStockQuantidade(stock.getQuantidadeAtual());
    }

    private void validateProdutoExists(Long produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new EntityNotFoundException("Product not found with id: " + produtoId);
        }
    }

    private void validateArmazemExists(Long armazemId) {
        if (!armazemRepository.existsById(armazemId)) {
            throw new EntityNotFoundException("Warehouse not found with id: " + armazemId);
        }
    }

    private void validateStockQuantidade(BigDecimal quantidade) {
        if (quantidade != null && quantidade.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Stock quantity cannot be negative");
        }
    }

    // ==================== MÉTODOS COM RETORNO DTO ====================

    public Page<StockDTO> filterDTO(StockFilter filter) {
        Page<Stock> stocks = stockRepository.filter(filter);
        return stocks.map(StockDTO::fromEntity);
    }

    public Page<StockDTO> findByArmazemIdDTO(Long armazemId, Pageable pageable) {
        Page<Stock> stocks = stockRepository.findByArmazemId(armazemId, pageable);
        return stocks.map(StockDTO::fromEntity);
    }

    public Page<StockResumoDTO> findStockResumoByEmpresaIdDTO(Long empresaId, Pageable pageable) {
        return stockRepository.findStockResumoByEmpresaId(empresaId, pageable)
                .map(this::toStockResumoDTO);
    }

    public List<StockDTO> findLowStockDTO(BigDecimal threshold) {
        List<Stock> stocks = stockRepository.findWithLowStock(threshold);
        return stocks.stream()
                .map(StockDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<StockDTO> findPositiveStockDTO() {
        List<Stock> stocks = stockRepository.findWithPositiveStock();
        return stocks.stream()
                .map(StockDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== CONVERSÃO PARA RESUMO DTO ====================

    private StockResumoDTO toStockResumoDTO(reset.reset.dto.projection.StockResumo resumo) {
        return StockResumoDTO.builder()
                .produtoId(resumo.getProdutoId())
                .produtoNome(resumo.getProdutoNome())
                .produtoCodigo(resumo.getProdutoCodigo())
                .armazemId(resumo.getArmazemId())
                .armazemNome(resumo.getArmazemNome())
                .quantidadeAtual(resumo.getQuantidadeAtual())
                .precoVenda(resumo.getPrecoVenda())
//                .valorTotal(resumo.getValorTotal())
                .build();
    }

    private StockResumoDTO toStockResumoFromEntity(Stock stock) {
        return StockResumoDTO.builder()
                .produtoId(stock.getProduto().getId())
                .produtoNome(stock.getProduto().getNome())
                .produtoCodigo(stock.getProduto().getCodigo())
                .armazemId(stock.getArmazem().getId())
                .armazemNome(stock.getArmazem().getNome())
                .quantidadeAtual(stock.getQuantidadeAtual())
                .precoVenda(stock.getProduto().getPrecoVenda())
                .valorTotal(stock.getQuantidadeAtual().multiply(stock.getProduto().getPrecoVenda()))
                .build();
    }

    // ==================== OPERAÇÕES DE STOCK ====================

    @Transactional
    public Stock adicionarStock(Long produtoId, Long armazemId, BigDecimal quantidade, String referencia) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        Armazem armazem = armazemRepository.findById(armazemId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }

        if (produto.getIsComposto() != null && produto.getIsComposto()) {
            throw new BusinessException("Cannot add stock directly to a composed product. Add stock to its components.");
        }

        Optional<Stock> stockOpt = stockRepository.findByProdutoIdAndArmazemId(produtoId, armazemId);
        Stock stock;

        if (stockOpt.isPresent()) {
            stock = stockOpt.get();
            stock.setQuantidadeAtual(stock.getQuantidadeAtual().add(quantidade));
        } else {
            stock = new Stock();
            stock.setProduto(produto);
            stock.setArmazem(armazem);
            stock.setQuantidadeAtual(quantidade);
        }

        stock = stockRepository.save(stock);

        MovimentoStock movimento = new MovimentoStock();
        movimento.setEmpresa(produto.getEmpresa());
        movimento.setProduto(produto);
        movimento.setArmazem(armazem);
        movimento.setTipo("ENTRADA_COMPRA");
        movimento.setQuantidade(quantidade);
        movimento.setReferencia(referencia);
        movimento.setDataMovimento(LocalDateTime.now());
        movimentoStockRepository.save(movimento);

        log.info("Stock added: Product {}, Quantity {}, Warehouse {}", produtoId, quantidade, armazemId);
        return stock;
    }

    @Transactional
    public Stock removerStock(Long produtoId, Long armazemId, BigDecimal quantidade, String referencia) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (produto.getIsComposto() != null && produto.getIsComposto()) {
            throw new BusinessException("Cannot remove stock directly from a composed product. Remove from its components.");
        }

        Stock stock = stockRepository.findByProdutoIdAndArmazemId(produtoId, armazemId)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found for product and warehouse"));

        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }

        if (stock.getQuantidadeAtual().compareTo(quantidade) < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + stock.getQuantidadeAtual() + ", Requested: " + quantidade
            );
        }

        stock.setQuantidadeAtual(stock.getQuantidadeAtual().subtract(quantidade));
        stock = stockRepository.save(stock);

        MovimentoStock movimento = new MovimentoStock();
        movimento.setEmpresa(stock.getProduto().getEmpresa());
        movimento.setProduto(stock.getProduto());
        movimento.setArmazem(stock.getArmazem());
        movimento.setTipo("SAIDA_VENDA");
        movimento.setQuantidade(quantidade);
        movimento.setReferencia(referencia);
        movimento.setDataMovimento(LocalDateTime.now());
        movimentoStockRepository.save(movimento);

        log.info("Stock removed: Product {}, Quantity {}, Warehouse {}", produtoId, quantidade, armazemId);
        return stock;
    }

    @Transactional
    public Stock ajustarStock(Long produtoId, Long armazemId, BigDecimal novaQuantidade, String motivo) {
        Stock stock = stockRepository.findByProdutoIdAndArmazemId(produtoId, armazemId)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found for product and warehouse"));

        if (novaQuantidade.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Stock quantity cannot be negative");
        }

        BigDecimal diferenca = novaQuantidade.subtract(stock.getQuantidadeAtual());
        stock.setQuantidadeAtual(novaQuantidade);
        stock = stockRepository.save(stock);

        MovimentoStock movimento = new MovimentoStock();
        movimento.setEmpresa(stock.getProduto().getEmpresa());
        movimento.setProduto(stock.getProduto());
        movimento.setArmazem(stock.getArmazem());
        movimento.setTipo("AJUSTE");
        movimento.setQuantidade(diferenca);
        movimento.setReferencia("Ajuste: " + motivo);
        movimento.setDataMovimento(LocalDateTime.now());
        movimentoStockRepository.save(movimento);

        log.info("Stock adjusted: Product {}, New Quantity {}, Warehouse {}", produtoId, novaQuantidade, armazemId);
        return stock;
    }

    @Transactional
    public Stock transferirStock(Long produtoId, Long origemArmazemId, Long destinoArmazemId,
                                 BigDecimal quantidade, String referencia) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (produto.getIsComposto() != null && produto.getIsComposto()) {
            throw new BusinessException("Cannot transfer stock of a composed product. Transfer its components.");
        }

        removerStock(produtoId, origemArmazemId, quantidade, "Transferência: " + referencia);
        return adicionarStock(produtoId, destinoArmazemId, quantidade, "Transferência: " + referencia);
    }

    // ==================== CONSULTAS DE STOCK ====================

    public Stock getStockByProdutoAndArmazem(Long produtoId, Long armazemId) {
        return stockRepository.findByProdutoIdAndArmazemId(produtoId, armazemId)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));
    }

    public BigDecimal getQuantidadeTotalPorProduto(Long produtoId) {
        List<Stock> stocks = stockRepository.findByProdutoId(produtoId);
        return stocks.stream()
                .map(Stock::getQuantidadeAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getQuantidadeTotalPorProdutoComposto(Long produtoCompostoId) {
        Produto produto = produtoRepository.findById(produtoCompostoId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (!produto.getIsComposto()) {
            throw new BusinessException("Product is not a composed product");
        }

        BigDecimal menorStock = null;
        for (ProdutoCompostoItem item : produto.getItensComposto()) {
            BigDecimal stockDisponivel = getQuantidadeTotalPorProduto(item.getProdutoFilho().getId());
            BigDecimal quantidadeNecessaria = item.getQuantidade();
            BigDecimal quantidadePossivel = stockDisponivel.divide(quantidadeNecessaria, 2, BigDecimal.ROUND_DOWN);

            if (menorStock == null || quantidadePossivel.compareTo(menorStock) < 0) {
                menorStock = quantidadePossivel;
            }
        }
        return menorStock != null ? menorStock : BigDecimal.ZERO;
    }

    public Page<Stock> filter(StockFilter filter) {
        return stockRepository.filter(filter);
    }

    public Page<Stock> findByArmazemId(Long armazemId, Pageable pageable) {
        return stockRepository.findByArmazemId(armazemId, pageable);
    }

    public List<Stock> findWithLowStock(BigDecimal threshold) {
        return stockRepository.findWithLowStock(threshold);
    }

    public List<Stock> findWithPositiveStock() {
        return stockRepository.findWithPositiveStock();
    }

    public Page<reset.reset.dto.projection.StockResumo> findStockResumoByEmpresaId(Long empresaId, Pageable pageable) {
        return stockRepository.findStockResumoByEmpresaId(empresaId, pageable);
    }

    // ==================== CONTROLE DE STOCK PARA PEDIDOS ====================

    public boolean verificarStockParaPedido(List<PedidoItemRequest> itens) {
        for (PedidoItemRequest item : itens) {
            Produto produto = produtoRepository.findById(item.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            if (produto.getIsComposto() != null && produto.getIsComposto()) {
                for (ProdutoCompostoItem composto : produto.getItensComposto()) {
                    BigDecimal quantidadeNecessaria = composto.getQuantidade().multiply(item.getQuantidade());
                    BigDecimal stockDisponivel = getQuantidadeTotalPorProduto(composto.getProdutoFilho().getId());

                    if (stockDisponivel.compareTo(quantidadeNecessaria) < 0) {
                        throw new InsufficientStockException(
                                "Stock insuficiente para " + composto.getProdutoFilho().getNome() +
                                        " (necessário: " + quantidadeNecessaria + ", disponível: " + stockDisponivel + ")"
                        );
                    }
                }
            } else {
                BigDecimal stockDisponivel = getQuantidadeTotalPorProduto(produto.getId());
                if (stockDisponivel.compareTo(item.getQuantidade()) < 0) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para " + produto.getNome() +
                                    " (necessário: " + item.getQuantidade() + ", disponível: " + stockDisponivel + ")"
                    );
                }
            }
        }
        return true;
    }

    @Transactional
    public void baixarStockParaPedido(List<PedidoItemRequest> itens, Long armazemId, String referencia) {
        if (itens == null || itens.isEmpty()) {
            throw new BusinessException("Lista de itens vazia");
        }

        if (!armazemRepository.existsById(armazemId)) {
            throw new EntityNotFoundException("Armazém não encontrado com id: " + armazemId);
        }

        for (PedidoItemRequest item : itens) {
            Produto produto = produtoRepository.findById(item.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + item.getItemId()));

            if (produto.getIsComposto() != null && produto.getIsComposto()) {
                for (ProdutoCompostoItem composto : produto.getItensComposto()) {
                    BigDecimal quantidadeBaixar = composto.getQuantidade().multiply(item.getQuantidade());
                    removerStock(composto.getProdutoFilho().getId(), armazemId, quantidadeBaixar,
                            "Pedido: " + referencia + " - " + produto.getNome());
                }
            } else {
                removerStock(produto.getId(), armazemId, item.getQuantidade(),
                        "Pedido: " + referencia);
            }
        }

        log.info("Stock baixado para pedido: {}", referencia);
    }

    public boolean verificarStockProduto(Long produtoId, BigDecimal quantidade) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        if (produto.getIsComposto() != null && produto.getIsComposto()) {
            BigDecimal stockDisponivel = getQuantidadeTotalPorProdutoComposto(produtoId);
            return stockDisponivel.compareTo(quantidade) >= 0;
        } else {
            BigDecimal stockDisponivel = getQuantidadeTotalPorProduto(produtoId);
            return stockDisponivel.compareTo(quantidade) >= 0;
        }
    }

    public BigDecimal calcularCustoPedido(List<PedidoItemRequest> itens) {
        BigDecimal custoTotal = BigDecimal.ZERO;

        for (PedidoItemRequest item : itens) {
            Produto produto = produtoRepository.findById(item.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            if (produto.getIsComposto() != null && produto.getIsComposto()) {
                for (ProdutoCompostoItem composto : produto.getItensComposto()) {
                    BigDecimal custoItem = composto.getProdutoFilho().getPrecoCusto()
                            .multiply(composto.getQuantidade())
                            .multiply(item.getQuantidade());
                    custoTotal = custoTotal.add(custoItem);
                }
            } else {
                custoTotal = custoTotal.add(
                        produto.getPrecoCusto().multiply(item.getQuantidade())
                );
            }
        }

        return custoTotal;
    }

    public BigDecimal calcularValorPedido(List<PedidoItemRequest> itens) {
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (PedidoItemRequest item : itens) {
            Produto produto = produtoRepository.findById(item.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            if (produto.getIsComposto() != null && produto.getIsComposto()) {
                BigDecimal valorBase = produto.getPrecoVenda();
                BigDecimal adicionais = BigDecimal.ZERO;
                for (ProdutoCompostoItem composto : produto.getItensComposto()) {
                    if (composto.getObrigatorio() || composto.getPrecoAdicional().compareTo(BigDecimal.ZERO) > 0) {
                        adicionais = adicionais.add(composto.getPrecoAdicional());
                    }
                }
                valorTotal = valorTotal.add(valorBase.add(adicionais).multiply(item.getQuantidade()));
            } else {
                valorTotal = valorTotal.add(
                        produto.getPrecoVenda().multiply(item.getQuantidade())
                );
            }
        }

        return valorTotal;
    }
}