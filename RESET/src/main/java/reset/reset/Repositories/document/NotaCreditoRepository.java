package reset.reset.Repositories.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.Tipos.NotaCredito;
import reset.reset.Repositories.BaseRepository;

@Repository
public interface NotaCreditoRepository extends BaseRepository<NotaCredito, Long> {

    Page<NotaCredito> findByEmpresaId(Long empresaId, Pageable pageable);

    boolean existsByDocumentoOrigemId(Long documentoOrigemId);
}
