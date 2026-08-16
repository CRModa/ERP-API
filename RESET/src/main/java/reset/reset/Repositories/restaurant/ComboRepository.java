package reset.reset.Repositories.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.restaurant.Combo;
import reset.reset.Repositories.BaseRepository;

import java.util.List;

@Repository
public interface ComboRepository extends BaseRepository<Combo, Long> {

    @Query("SELECT c FROM Combo c WHERE c.empresa.id = :empresaId AND c.ativo = true")
    Page<Combo> findActiveByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM Combo c WHERE c.empresa.id = :empresaId")
    Page<Combo> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM Combo c WHERE c.empresa.id = :empresaId AND c.nome LIKE %:nome%")
    List<Combo> searchByNome(@Param("empresaId") Long empresaId, @Param("nome") String nome);
}
