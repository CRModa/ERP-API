package reset.reset.Repositories.document;

import org.springframework.stereotype.Repository;
import reset.reset.Models.financial.VD;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VdRepository extends BaseRepository<VD, Long> {
    boolean existsByVendaDocumentoId(Long vendaDocumentoId);

    Optional<VD> findByVendaDocumentoId(Long vendaDocumentoId);

    List<VD> findAllByVendaDocumentoId(Long vendaDocumentoId);

    List<VD> findAllByReciboDocumentoId(Long reciboDocumentoId);
}
