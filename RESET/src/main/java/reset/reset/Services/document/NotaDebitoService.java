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
import reset.reset.dto.document.NotaDebitoDTO;
import reset.reset.dto.request.NotaDebitoRequest;

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

        Documento originalDoc = documentoRepository.findById(notaDebito.getDocumentoOrigemId())
                .orElseThrow(() -> new EntityNotFoundException("Original document not found"));

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
        Documento originalDoc = documentoRepository.findById(notaDebito.getDocumentoOrigemId()).orElse(null);
        if (originalDoc != null && notaDebito.getTotal() == null) {
            notaDebito.setTotal(originalDoc.getTotal());
        }
        return super.save(notaDebito);
    }

    // ==================== MÉTODOS COM RETORNO DTO ====================

    @Transactional
    public NotaDebitoDTO createNotaDebito(NotaDebitoRequest request) {
        NotaDebito nota = request.toEntity();
        NotaDebito saved = save(nota);
        return NotaDebitoDTO.fromEntity(saved);
    }

    public NotaDebitoDTO findByIdDTO(Long id) {
        NotaDebito nota = findByIdOrThrow(id);
        return NotaDebitoDTO.fromEntity(nota);
    }

    public Page<NotaDebitoDTO> findAllDTO(Pageable pageable) {
        Page<NotaDebito> notas = findAll(pageable);
        return notas.map(NotaDebitoDTO::fromEntity);
    }

    public Page<NotaDebitoDTO> findByEmpresaIdDTO(Long empresaId, Pageable pageable) {
        Page<NotaDebito> notas = notaDebitoRepository.findByEmpresaId(empresaId, pageable);
        return notas.map(NotaDebitoDTO::fromEntity);
    }

    public NotaDebitoDTO findByDocumentoOrigemIdDTO(Long documentoOrigemId) {
        NotaDebito nota = notaDebitoRepository.findByDocumentoOrigemId(documentoOrigemId)
                .orElseThrow(() -> new EntityNotFoundException("Debit note not found for document: " + documentoOrigemId));
        return NotaDebitoDTO.fromEntity(nota);
    }

    @Transactional
    public NotaDebitoDTO aprovarNotaDebitoDTO(Long id) {
        NotaDebito nota = aprovarNotaDebito(id);
        return NotaDebitoDTO.fromEntity(nota);
    }

    @Transactional
    public NotaDebitoDTO rejeitarNotaDebitoDTO(Long id, String motivo) {
        NotaDebito nota = rejeitarNotaDebito(id, motivo);
        return NotaDebitoDTO.fromEntity(nota);
    }

    // ==================== MÉTODOS ORIGINAIS (MANTIDOS) ====================

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
