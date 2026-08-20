package reset.reset.Repositories.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.product.Produto;
import reset.reset.Repositories.BaseRepository;
import reset.reset.Repositories.specification.FilterOperation;
import reset.reset.Repositories.specification.ProdutoSpecification;
import reset.reset.dto.filter.ProdutoFilter;
import reset.reset.dto.projection.ProdutoResumo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends BaseRepository<Produto, Long> {

    Optional<Produto> findByCodigo(String codigo);
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    List<Produto> findByCategoriaId(Long categoriaId);

    @Query("SELECT p FROM Produto p WHERE p.empresa.id = :empresaId")
    Page<Produto> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.empresa.id = :empresaId AND p.ativo = true")
    Page<Produto> findActiveByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.empresa.id = :empresaId AND p.categoria.id = :categoriaId")
    Page<Produto> findByEmpresaIdAndCategoriaId(@Param("empresaId") Long empresaId,
                                                @Param("categoriaId") Long categoriaId,
                                                Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.precoVenda BETWEEN :minPrice AND :maxPrice")
    List<Produto> findProdutosByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                           @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Produto p WHERE p.iva.id = :ivaId")
    List<Produto> findByIvaId(@Param("ivaId") Long ivaId);

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.empresa.id = :empresaId AND p.ativo = true")
    long countActiveByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Produto p WHERE p.empresa.id = :empresaId AND p.ativo = true ORDER BY p.nome ASC")
    List<Produto> findActiveByEmpresaIdOrderByNome(@Param("empresaId") Long empresaId);

    @Query("SELECT p.id as id, p.codigo as codigo, p.nome as nome, p.precoVenda as precoVenda, " +
            "p.precoCusto as precoCusto, c.descricao as categoriaNome " +
            "FROM Produto p JOIN p.categoria c WHERE p.empresa.id = :empresaId AND p.ativo = true")
    Page<ProdutoResumo> findProdutoResumoByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    default Page<Produto> filter(ProdutoFilter filter) {
        ProdutoSpecification spec = new ProdutoSpecification();
        spec.addFilter("codigo", filter.getCodigo(), FilterOperation.LIKE);
        spec.addFilter("nome", filter.getNome(), FilterOperation.LIKE);
        spec.addFilter("descricao", filter.getDescricao(), FilterOperation.LIKE);
        spec.addFilter("empresa.id", filter.getEmpresaId(), FilterOperation.EQUALS);
        spec.addFilter("categoria.id", filter.getCategoriaId(), FilterOperation.EQUALS);
        spec.addFilter("iva.id", filter.getIvaId(), FilterOperation.EQUALS);
        spec.addBetween("precoVenda", filter.getPrecoVendaMinimo(), filter.getPrecoVendaMaximo());
        spec.addBetween("precoCusto", filter.getPrecoCustoMinimo(), filter.getPrecoCustoMaximo());
        spec.addDateTimeRange("dataRegisto", filter.getDataInicio(), filter.getDataFim());
        if (filter.getAtivo() != null) {
            spec.addFilter("ativo", filter.getAtivo(), FilterOperation.EQUALS);
        }
        return findAll(spec, filter.toPageable());
    }

    // ==================== MÉTODOS PARA RESTAURANTE ====================

    @Query("SELECT DISTINCT p FROM Produto p " +
            "JOIN p.categoria c " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.ativo = true " +
            "AND p.disponivel = true " +
            "AND c.visivelRestaurante = true")
    List<Produto> findByEmpresaIdAndCategoriaVisivelRestaurante(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Produto p " +
            "WHERE p.categoria.id = :categoriaId " +
            "AND p.ativo = true " +
            "AND p.disponivel = true")
    List<Produto> findByCategoriaIdAndDisponivelTrue(@Param("categoriaId") Long categoriaId);

    @Query("SELECT p FROM Produto p " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.ativo = true " +
            "AND p.disponivel = true " +
            "AND p.destaque = true")
    List<Produto> findDestaquesByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Produto p " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.ativo = true " +
            "AND p.isComposto = true")
    List<Produto> findCompostosByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Produto p " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.isComposto = false")
    List<Produto> findSimplesByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Produto p " +
            "WHERE p.empresa.id = :empresaId " +
            "AND p.ativo = true " +
            "AND p.isComposto = true " +
            "AND p.disponivel = true")
    List<Produto> findCompostosDisponiveisByEmpresaId(@Param("empresaId") Long empresaId);
}

