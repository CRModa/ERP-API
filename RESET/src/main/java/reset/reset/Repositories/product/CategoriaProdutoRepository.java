package reset.reset.Repositories.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.product.CategoriaProduto;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaProdutoRepository extends BaseRepository<CategoriaProduto, Long> {

    Optional<CategoriaProduto> findByCodigo(String codigo);

    Optional<CategoriaProduto> findByCodigoAndEmpresaId(String codigo, Long empresaId);

    List<CategoriaProduto> findByDescricaoContainingIgnoreCase(String descricao);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId")
    Page<CategoriaProduto> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId AND c.ativo = true")
    Page<CategoriaProduto> findActiveByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId ORDER BY c.descricao ASC")
    List<CategoriaProduto> findAllByEmpresaIdOrderByDescricao(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId ORDER BY c.descricao ASC")
    List<CategoriaProduto> findAllByEmpresaIdOrderByOrdem(@Param("empresaId") Long empresaId);

    boolean existsByCodigoAndEmpresaId(String codigo, Long empresaId);

    // Métodos para visibilidade por canal

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId AND c.visivelRestaurante = true AND c.ativo = true ORDER BY c.descricao ASC")
    List<CategoriaProduto> findByEmpresaIdAndVisivelRestauranteTrue(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId AND c.visivelPos = true AND c.ativo = true ORDER BY c.descricao ASC")
    List<CategoriaProduto> findByEmpresaIdAndVisivelPosTrue(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId AND c.visivelFarmacia = true AND c.ativo = true ORDER BY c.descricao ASC")
    List<CategoriaProduto> findByEmpresaIdAndVisivelFarmaciaTrue(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId AND c.visivelWeb = true AND c.ativo = true ORDER BY c.descricao ASC")
    List<CategoriaProduto> findByEmpresaIdAndVisivelWebTrue(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId AND c.ativo = true ORDER BY c.descricao ASC")
    List<CategoriaProduto> findByEmpresaIdAndVisivelPdvTrue(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId AND (c.visivelRestaurante = true OR c.visivelPos = true OR c.visivelFarmacia = true OR c.visivelWeb = true) AND c.ativo = true")
    List<CategoriaProduto> findVisiveisEmAlgumCanal(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId AND c.visivelRestaurante = true AND c.ativo = true AND c.id = :categoriaId")
    Optional<CategoriaProduto> findByIdAndVisivelRestauranteTrue(@Param("categoriaId") Long categoriaId);

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.categoria.id = :categoriaId AND p.ativo = true")
    Long countProdutosAtivosByCategoriaId(@Param("categoriaId") Long categoriaId);
}