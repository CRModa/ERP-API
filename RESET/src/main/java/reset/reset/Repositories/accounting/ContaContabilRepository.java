package reset.reset.Repositories.accounting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.accounting.ContaContabil;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContaContabilRepository extends BaseRepository<ContaContabil, Long> {

    Optional<ContaContabil> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    List<ContaContabil> findByTipo(String tipo);

    @Query("SELECT c FROM ContaContabil c WHERE c.empresa.id = :empresaId")
    Page<ContaContabil> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM ContaContabil c WHERE c.empresa.id = :empresaId AND c.tipo = :tipo")
    List<ContaContabil> findByEmpresaIdAndTipo(@Param("empresaId") Long empresaId, @Param("tipo") String tipo);

    @Query("SELECT c FROM ContaContabil c WHERE c.empresa.id = :empresaId ORDER BY c.codigo ASC")
    List<ContaContabil> findAllByEmpresaIdOrderByCodigo(@Param("empresaId") Long empresaId);
}