package reset.reset.Repositories.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.Tipos.GuiaTransporte;
import reset.reset.Repositories.BaseRepository;

@Repository
public interface GuiaTransporteRepository extends BaseRepository<GuiaTransporte, Long> {
    Page<GuiaTransporte> findByEmpresaId(Long empresaId, Pageable pageable);

    Page<GuiaTransporte> findByEstado(String estado, Pageable pageable);
}
