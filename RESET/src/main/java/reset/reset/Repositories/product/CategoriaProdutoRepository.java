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
    List<CategoriaProduto> findByDescricaoContainingIgnoreCase(String descricao);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId")
    Page<CategoriaProduto> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM CategoriaProduto c WHERE c.empresa.id = :empresaId ORDER BY c.descricao ASC")
    List<CategoriaProduto> findAllByEmpresaIdOrderByDescricao(@Param("empresaId") Long empresaId);

    boolean existsByCodigoAndEmpresaId(String codigo, Long empresaId);
}
