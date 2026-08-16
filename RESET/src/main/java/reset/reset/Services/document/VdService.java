package reset.reset.Services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.document.Documento;
import reset.reset.Models.financial.VD;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Repositories.document.VdRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VdService {

    private final VdRepository vdRepository;
    private final DocumentoRepository documentoRepository;

    @Transactional
    public VD associarVendaRecibo(Long vendaDocumentoId, Long reciboDocumentoId) {
        Documento venda = documentoRepository.findById(vendaDocumentoId)
                .orElseThrow(() -> new EntityNotFoundException("Venda document not found"));

        Documento recibo = documentoRepository.findById(reciboDocumentoId)
                .orElseThrow(() -> new EntityNotFoundException("Recibo document not found"));

        // Verificar se já existe associação
        if (vdRepository.existsByVendaDocumentoId(vendaDocumentoId)) {
            throw new BusinessException("This sale document already has an associated receipt");
        }

        VD vd = new VD();
        vd.setVendaDocumento(venda);
        vd.setReciboDocumento(recibo);

        return vdRepository.save(vd);
    }

    @Transactional
    public void desassociarVendaRecibo(Long vendaDocumentoId) {
        VD vd = vdRepository.findByVendaDocumentoId(vendaDocumentoId)
                .orElseThrow(() -> new EntityNotFoundException("Association not found"));
        vdRepository.delete(vd);
    }

    public List<VD> findByVendaDocumentoId(Long vendaDocumentoId) {
        return vdRepository.findAllByVendaDocumentoId(vendaDocumentoId);
    }

    public List<VD> findByReciboDocumentoId(Long reciboDocumentoId) {
        return vdRepository.findAllByReciboDocumentoId(reciboDocumentoId);
    }
}
