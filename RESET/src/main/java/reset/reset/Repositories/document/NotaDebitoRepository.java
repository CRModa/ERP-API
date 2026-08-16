package reset.reset.Repositories.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.Tipos.NotaDebito;
import reset.reset.Repositories.BaseRepository;

@Repository
public interface NotaDebitoRepository extends BaseRepository<NotaDebito, Long> {

    Page<NotaDebito> findByEmpresaId(Long empresaId, Pageable pageable);

    boolean existsByDocumentoOrigemId(Long documentoOrigemId);
}
