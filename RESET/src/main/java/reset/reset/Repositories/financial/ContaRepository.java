package reset.reset.Repositories.financial;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.financial.Conta;
import reset.reset.Repositories.BaseRepository;

import java.util.List;

@Repository
public interface ContaRepository extends BaseRepository<Conta, Long> {

    @Query("SELECT c FROM Conta c WHERE c.empresa.id = :empresaId")
    Page<Conta> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM Conta c WHERE c.tipo = :tipo")
    List<Conta> findByTipo(@Param("tipo") String tipo);

    @Query("SELECT c FROM Conta c WHERE c.empresa.id = :empresaId AND c.tipo = :tipo")
    List<Conta> findByEmpresaIdAndTipo(@Param("empresaId") Long empresaId, @Param("tipo") String tipo);
}
