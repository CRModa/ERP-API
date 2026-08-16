package reset.reset.Services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.document.Documento;
import reset.reset.Models.document.Tipos.NotaCredito;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Repositories.document.NotaCreditoRepository;
import reset.reset.Services.base.BaseServiceImpl;

@Service
@Slf4j
public class NotaCreditoService extends BaseServiceImpl<NotaCredito, Long, NotaCreditoRepository> {

    private final NotaCreditoRepository notaCreditoRepository;
    @Autowired
    private DocumentoRepository documentoRepository;

    public NotaCreditoService(NotaCreditoRepository repository) {
        super(repository);
        this.notaCreditoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(NotaCredito notaCredito) {
        if (notaCredito.getDocumentoOrigemId() == null) {
            throw new BusinessException("Original document ID is required");
        }

        // Validate original document exists
        Documento originalDoc = documentoRepository.findById(notaCredito.getDocumentoOrigemId())
                .orElseThrow(() -> new EntityNotFoundException("Original document not found"));

        // Check if credit note already exists for this document
        if (notaCreditoRepository.existsByDocumentoOrigemId(notaCredito.getDocumentoOrigemId())) {
            throw new BusinessException("A credit note already exists for this document");
        }

        if (notaCredito.getMotivo() == null || notaCredito.getMotivo().isEmpty()) {
            throw new BusinessException("Reason is required");
        }
    }

    @Override
    @Transactional
    public NotaCredito save(NotaCredito notaCredito) {
        if (notaCredito.getEstado() == null) {
            notaCredito.setEstado("PENDENTE");
        }
        // Set the total from original document or calculate
        Documento originalDoc = documentoRepository.findById(notaCredito.getDocumentoOrigemId()).orElse(null);
        if (originalDoc != null && notaCredito.getTotal() == null) {
            notaCredito.setTotal(originalDoc.getTotal());
        }
        return super.save(notaCredito);
    }

    @Transactional
    public NotaCredito aprovarNotaCredito(Long id) {
        NotaCredito nota = findByIdOrThrow(id);

        if (!"PENDENTE".equals(nota.getEstado())) {
            throw new BusinessException("Only pending credit notes can be approved");
        }

        nota.setEstado("APROVADO");
        return notaCreditoRepository.save(nota);
    }

    @Transactional
    public NotaCredito rejeitarNotaCredito(Long id, String motivo) {
        NotaCredito nota = findByIdOrThrow(id);

        if (!"PENDENTE".equals(nota.getEstado())) {
            throw new BusinessException("Only pending credit notes can be rejected");
        }

        nota.setEstado("REJEITADO");
        nota.setMotivo(nota.getMotivo() + " (Rejeitado: " + motivo + ")");
        return notaCreditoRepository.save(nota);
    }

    public Page<NotaCredito> findByEmpresaId(Long empresaId, Pageable pageable) {
        return notaCreditoRepository.findByEmpresaId(empresaId, pageable);
    }
}
