package reset.reset.Services.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.document.Documento;
import reset.reset.Models.document.Tipos.NotaDebito;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Repositories.document.NotaDebitoRepository;
import reset.reset.Services.base.BaseServiceImpl;

@Service
@Slf4j
public class NotaDebitoService extends BaseServiceImpl<NotaDebito, Long, NotaDebitoRepository> {

    private final NotaDebitoRepository notaDebitoRepository;
    @Autowired
    private DocumentoRepository documentoRepository;

    public NotaDebitoService(NotaDebitoRepository repository) {
        super(repository);
        this.notaDebitoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(NotaDebito notaDebito) {
        if (notaDebito.getDocumentoOrigemId() == null) {
            throw new BusinessException("Original document ID is required");
        }

        // Validate original document exists
        Documento originalDoc = documentoRepository.findById(notaDebito.getDocumentoOrigemId())
                .orElseThrow(() -> new EntityNotFoundException("Original document not found"));

        // Check if debit note already exists for this document
        if (notaDebitoRepository.existsByDocumentoOrigemId(notaDebito.getDocumentoOrigemId())) {
            throw new BusinessException("A debit note already exists for this document");
        }

        if (notaDebito.getMotivo() == null || notaDebito.getMotivo().isEmpty()) {
            throw new BusinessException("Reason is required");
        }
    }

    @Override
    @Transactional
    public NotaDebito save(NotaDebito notaDebito) {
        if (notaDebito.getEstado() == null) {
            notaDebito.setEstado("PENDENTE");
        }
        // Set the total from original document or calculate
        Documento originalDoc = documentoRepository.findById(notaDebito.getDocumentoOrigemId()).orElse(null);
        if (originalDoc != null && notaDebito.getTotal() == null) {
            notaDebito.setTotal(originalDoc.getTotal());
        }
        return super.save(notaDebito);
    }

    @Transactional
    public NotaDebito aprovarNotaDebito(Long id) {
        NotaDebito nota = findByIdOrThrow(id);

        if (!"PENDENTE".equals(nota.getEstado())) {
            throw new BusinessException("Only pending debit notes can be approved");
        }

        nota.setEstado("APROVADO");
        return notaDebitoRepository.save(nota);
    }

    @Transactional
    public NotaDebito rejeitarNotaDebito(Long id, String motivo) {
        NotaDebito nota = findByIdOrThrow(id);

        if (!"PENDENTE".equals(nota.getEstado())) {
            throw new BusinessException("Only pending debit notes can be rejected");
        }

        nota.setEstado("REJEITADO");
        nota.setMotivo(nota.getMotivo() + " (Rejeitado: " + motivo + ")");
        return notaDebitoRepository.save(nota);
    }

    public Page<NotaDebito> findByEmpresaId(Long empresaId, Pageable pageable) {
        return notaDebitoRepository.findByEmpresaId(empresaId, pageable);
    }
}
