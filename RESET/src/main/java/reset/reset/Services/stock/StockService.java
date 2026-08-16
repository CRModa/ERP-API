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
import reset.reset.Models.stock.Armazem;
import reset.reset.Models.stock.MovimentoStock;
import reset.reset.Models.stock.Stock;
import reset.reset.Repositories.product.ProdutoRepository;
import reset.reset.Repositories.stock.ArmazemRepository;
import reset.reset.Repositories.stock.MovimentoStockRepository;
import reset.reset.Repositories.stock.StockRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.StockFilter;
import reset.reset.dto.projection.StockResumo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Transactional
    public Stock adicionarStock(Long produtoId, Long armazemId, BigDecimal quantidade, String referencia) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        Armazem armazem = armazemRepository.findById(armazemId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
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

        // Registrar movimento
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

        // Registrar movimento
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

        // Registrar movimento
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
        // Remover do armazém de origem
        removerStock(produtoId, origemArmazemId, quantidade, "Transferência: " + referencia);

        // Adicionar ao armazém de destino
        return adicionarStock(produtoId, destinoArmazemId, quantidade, "Transferência: " + referencia);
    }

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

    public Page<StockResumo> findStockResumoByEmpresaId(Long empresaId, Pageable pageable) {
        return stockRepository.findStockResumoByEmpresaId(empresaId, pageable);
    }
}
