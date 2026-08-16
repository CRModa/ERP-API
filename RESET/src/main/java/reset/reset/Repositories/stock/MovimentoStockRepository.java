package reset.reset.Repositories.stock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.stock.MovimentoStock;
import reset.reset.Repositories.BaseRepository;
import reset.reset.Repositories.specification.FilterOperation;
import reset.reset.Repositories.specification.MovimentoStockSpecification;
import reset.reset.dto.filter.MovimentoStockFilter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentoStockRepository extends BaseRepository<MovimentoStock, Long> {

    @Query("SELECT m FROM MovimentoStock m WHERE m.produto.id = :produtoId ORDER BY m.dataMovimento DESC")
    List<MovimentoStock> findByProdutoIdOrderByDataMovimentoDesc(@Param("produtoId") Long produtoId);

    @Query("SELECT m FROM MovimentoStock m WHERE m.armazem.id = :armazemId ORDER BY m.dataMovimento DESC")
    Page<MovimentoStock> findByArmazemId(@Param("armazemId") Long armazemId, Pageable pageable);

    @Query("SELECT m FROM MovimentoStock m WHERE m.tipo = :tipo")
    List<MovimentoStock> findByTipo(@Param("tipo") String tipo);

    @Query("SELECT m FROM MovimentoStock m WHERE m.referencia = :referencia")
    List<MovimentoStock> findByReferencia(@Param("referencia") String referencia);

    @Query("SELECT m FROM MovimentoStock m WHERE m.produto.id = :produtoId AND m.dataMovimento BETWEEN :dataInicio AND :dataFim")
    List<MovimentoStock> findMovimentosByProdutoAndPeriodo(@Param("produtoId") Long produtoId,
                                                           @Param("dataInicio") LocalDateTime dataInicio,
                                                           @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT m FROM MovimentoStock m WHERE m.empresa.id = :empresaId")
    Page<MovimentoStock> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT SUM(m.quantidade) FROM MovimentoStock m WHERE m.produto.id = :produtoId AND m.tipo = :tipo")
    BigDecimal sumQuantidadeByProdutoAndTipo(@Param("produtoId") Long produtoId, @Param("tipo") String tipo);

    default Page<MovimentoStock> filter(MovimentoStockFilter filter) {
        MovimentoStockSpecification spec = new MovimentoStockSpecification();
        spec.addFilter("empresa.id", filter.getEmpresaId(), FilterOperation.EQUALS);
        spec.addFilter("produto.id", filter.getProdutoId(), FilterOperation.EQUALS);
        spec.addFilter("armazem.id", filter.getArmazemId(), FilterOperation.EQUALS);
        spec.addFilter("tipo", filter.getTipo(), FilterOperation.EQUALS);
        spec.addFilter("referencia", filter.getReferencia(), FilterOperation.LIKE);
        spec.addBetween("quantidade", filter.getQuantidadeMinima(), filter.getQuantidadeMaxima());
        spec.addDateTimeRange("dataMovimento", filter.getDataInicio(), filter.getDataFim());
        return findAll(spec, filter.toPageable());
    }
}

