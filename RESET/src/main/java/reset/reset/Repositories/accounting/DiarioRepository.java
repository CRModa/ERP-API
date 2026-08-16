package reset.reset.Repositories.accounting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.accounting.Diario;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiarioRepository extends BaseRepository<Diario, Long> {

    Optional<Diario> findByCodigo(String codigo);

    @Query("SELECT d FROM Diario d WHERE d.empresa.id = :empresaId")
    Page<Diario> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT d FROM Diario d WHERE d.empresa.id = :empresaId ORDER BY d.codigo ASC")
    List<Diario> findAllByEmpresaIdOrderByCodigo(@Param("empresaId") Long empresaId);
}
