package reset.reset.Repositories.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.restaurant.Mesa;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MesaRepository extends BaseRepository<Mesa, Long> {

    Optional<Mesa> findByEmpresaIdAndNumero(Long empresaId, String numero);

    @Query("SELECT m FROM Mesa m WHERE m.empresa.id = :empresaId AND m.status = :status")
    List<Mesa> findByEmpresaIdAndStatus(@Param("empresaId") Long empresaId,
                                        @Param("status") Mesa.StatusMesa status);

    @Query("SELECT m FROM Mesa m WHERE m.empresa.id = :empresaId AND m.ativo = true")
    Page<Mesa> findActiveByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT m FROM Mesa m WHERE m.empresa.id = :empresaId AND m.status = 'DISPONIVEL' AND m.ativo = true")
    List<Mesa> findMesasDisponiveis(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(m) FROM Mesa m WHERE m.empresa.id = :empresaId AND m.status = 'OCUPADA'")
    Long countMesasOcupadas(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(m) FROM Mesa m WHERE m.empresa.id = :empresaId AND m.ativo = true")
    Long countActiveByEmpresaId(@Param("empresaId") Long empresaId);
}