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
import reset.reset.Models.document.Tipos.NotaCredito;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Repositories.document.NotaCreditoRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.document.NotaCreditoDTO;
import reset.reset.dto.request.NotaCreditoRequest;

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

        Documento originalDoc = documentoRepository.findById(notaCredito.getDocumentoOrigemId())
                .orElseThrow(() -> new EntityNotFoundException("Original document not found"));

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
        Documento originalDoc = documentoRepository.findById(notaCredito.getDocumentoOrigemId()).orElse(null);
        if (originalDoc != null && notaCredito.getTotal() == null) {
            notaCredito.setTotal(originalDoc.getTotal());
        }
        return super.save(notaCredito);
    }

    // ==================== MÉTODOS COM RETORNO DTO ====================

    @Transactional
    public NotaCreditoDTO createNotaCredito(NotaCreditoRequest request) {
        NotaCredito nota = request.toEntity();
        NotaCredito saved = save(nota);
        return NotaCreditoDTO.fromEntity(saved);
    }

    public NotaCreditoDTO findByIdDTO(Long id) {
        NotaCredito nota = findByIdOrThrow(id);
        return NotaCreditoDTO.fromEntity(nota);
    }

    public Page<NotaCreditoDTO> findAllDTO(Pageable pageable) {
        Page<NotaCredito> notas = findAll(pageable);
        return notas.map(NotaCreditoDTO::fromEntity);
    }

    public Page<NotaCreditoDTO> findByEmpresaIdDTO(Long empresaId, Pageable pageable) {
        Page<NotaCredito> notas = notaCreditoRepository.findByEmpresaId(empresaId, pageable);
        return notas.map(NotaCreditoDTO::fromEntity);
    }

    public NotaCreditoDTO findByDocumentoOrigemIdDTO(Long documentoOrigemId) {
        NotaCredito nota = notaCreditoRepository.findByDocumentoOrigemId(documentoOrigemId)
                .orElseThrow(() -> new EntityNotFoundException("Credit note not found for document: " + documentoOrigemId));
        return NotaCreditoDTO.fromEntity(nota);
    }

    @Transactional
    public NotaCreditoDTO aprovarNotaCreditoDTO(Long id) {
        NotaCredito nota = aprovarNotaCredito(id);
        return NotaCreditoDTO.fromEntity(nota);
    }

    @Transactional
    public NotaCreditoDTO rejeitarNotaCreditoDTO(Long id, String motivo) {
        NotaCredito nota = rejeitarNotaCredito(id, motivo);
        return NotaCreditoDTO.fromEntity(nota);
    }

    // ==================== MÉTODOS ORIGINAIS (MANTIDOS) ====================

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
