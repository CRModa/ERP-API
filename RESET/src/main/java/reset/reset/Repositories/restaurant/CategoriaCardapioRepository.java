package reset.reset.Repositories.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.restaurant.CategoriaCardapio;
import reset.reset.Repositories.BaseRepository;

import java.util.List;

@Repository
public interface CategoriaCardapioRepository extends BaseRepository<CategoriaCardapio, Long> {

    @Query("SELECT c FROM CategoriaCardapio c WHERE c.empresa.id = :empresaId AND c.ativo = true ORDER BY c.ordem ASC")
    List<CategoriaCardapio> findActiveByEmpresaIdOrderByOrdem(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM CategoriaCardapio c WHERE c.empresa.id = :empresaId")
    Page<CategoriaCardapio> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    boolean existsByEmpresaIdAndNome(Long empresaId, String nome);
}
