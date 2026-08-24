package reset.reset.Repositories.stock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Models.stock.Stock;
import reset.reset.Repositories.BaseRepository;
import reset.reset.Repositories.specification.FilterOperation;
import reset.reset.Repositories.specification.StockSpecification;
import reset.reset.dto.filter.StockFilter;
import reset.reset.dto.projection.StockResumo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends BaseRepository<Stock, Long> {

    Optional<Stock> findByProdutoIdAndArmazemId(Long produtoId, Long armazemId);

    @Query("SELECT s FROM Stock s WHERE s.produto.id = :produtoId")
    List<Stock> findByProdutoId(@Param("produtoId") Long produtoId);

    @Query("SELECT s FROM Stock s WHERE s.armazem.id = :armazemId")
    Page<Stock> findByArmazemId(@Param("armazemId") Long armazemId, Pageable pageable);

    @Query("SELECT s FROM Stock s WHERE s.quantidadeAtual > 0")
    List<Stock> findWithPositiveStock();

    @Query("SELECT s FROM Stock s WHERE s.quantidadeAtual = 0")
    List<Stock> findWithZeroStock();

    @Query("SELECT s FROM Stock s WHERE s.quantidadeAtual < :threshold")
    List<Stock> findWithLowStock(@Param("threshold") BigDecimal threshold);

    @Query("SELECT s FROM Stock s JOIN s.produto p WHERE p.empresa.id = :empresaId")
    Page<Stock> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Stock s SET s.quantidadeAtual = :quantidade WHERE s.produto.id = :produtoId AND s.armazem.id = :armazemId")
    int updateQuantidade(@Param("produtoId") Long produtoId,
                         @Param("armazemId") Long armazemId,
                         @Param("quantidade") BigDecimal quantidade);

    @Query("SELECT s FROM Stock s JOIN s.produto p WHERE p.empresa.id = :empresaId AND p.ativo = true AND s.quantidadeAtual > 0")
    List<Stock> findAvailableStockByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p.id as produtoId, p.nome as produtoNome, p.codigo as produtoCodigo, " +
            "a.id as armazemId, a.nome as armazemNome, s.quantidadeAtual as quantidadeAtual, " +
            "p.precoVenda as precoVenda " +
            "FROM Stock s JOIN s.produto p JOIN s.armazem a " +
            "WHERE p.empresa.id = :empresaId AND s.quantidadeAtual > 0")
    Page<StockResumo> findStockResumoByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    default Page<Stock> filter(StockFilter filter) {
        StockSpecification spec = new StockSpecification();
        spec.addFilter("produto.empresa.id", filter.getEmpresaId(), FilterOperation.EQUALS);
        spec.addFilter("produto.id", filter.getProdutoId(), FilterOperation.EQUALS);
        spec.addFilter("armazem.id", filter.getArmazemId(), FilterOperation.EQUALS);
        spec.addBetween("quantidadeAtual", filter.getQuantidadeMinima(), filter.getQuantidadeMaxima());

        if (filter.getHasStock() != null && filter.getHasStock()) {
            spec.addFilter("quantidadeAtual", BigDecimal.ZERO, FilterOperation.GREATER_THAN);
        }

        if (filter.getLowStock() != null && filter.getLowStock()) {
            BigDecimal threshold = BigDecimal.valueOf(filter.getLowStockThreshold());
            spec.addFilter("quantidadeAtual", threshold, FilterOperation.LESS_THAN);
            spec.addFilter("quantidadeAtual", BigDecimal.ZERO, FilterOperation.GREATER_THAN);
        }

        return findAll(spec, filter.toPageable());
    }

    @Query("SELECT s FROM Stock s WHERE s.produto.id IN :produtoIds")
    List<Stock> findByProdutoIds(@Param("produtoIds") List<Long> produtoIds);

    @Query("SELECT s FROM Stock s WHERE s.produto.id = :produtoId AND s.quantidadeAtual > 0 ORDER BY s.quantidadeAtual DESC")
    List<Stock> findStockDisponivelByProdutoId(@Param("produtoId") Long produtoId);

    @Query("SELECT SUM(s.quantidadeAtual) FROM Stock s WHERE s.produto.id = :produtoId")
    BigDecimal sumQuantidadeByProdutoId(@Param("produtoId") Long produtoId);
}

