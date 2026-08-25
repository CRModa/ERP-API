package reset.reset.Services.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.DocumentoTipo;
import reset.reset.Repositories.document.DocumentoTipoRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.document.DocumentoTipoDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentoTipoService extends BaseServiceImpl<DocumentoTipo, Long, DocumentoTipoRepository> {

    private final DocumentoTipoRepository documentoTipoRepository;

    public DocumentoTipoService(DocumentoTipoRepository repository) {
        super(repository);
        this.documentoTipoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(DocumentoTipo tipo) {
        if (tipo.getDescricao() == null || tipo.getDescricao().isEmpty()) {
            throw new BusinessException("Description is required");
        }
        if (tipo.getClasse() == null) {
            throw new BusinessException("Class is required");
        }
        validateDescricaoUniqueness(tipo.getDescricao(), null);
    }

    @Override
    protected void validateBeforeUpdate(Long id, DocumentoTipo tipo) {
        DocumentoTipo existing = findByIdOrThrow(id);
        if (!existing.getDescricao().equals(tipo.getDescricao())) {
            validateDescricaoUniqueness(tipo.getDescricao(), id);
        }
        if (tipo.getClasse() == null) {
            throw new BusinessException("Class is required");
        }
    }

    private void validateDescricaoUniqueness(String descricao, Long excludeId) {
        documentoTipoRepository.findByDescricao(descricao)
                .ifPresent(tipo -> {
                    if (excludeId == null || !tipo.getId().equals(excludeId)) {
                        throw new BusinessException("Document type with description '" + descricao + "' already exists");
                    }
                });
    }

    @Override
    @Transactional
    public DocumentoTipo save(DocumentoTipo tipo) {
        if (tipo.getNumeracaoAutomatica() == null) {
            tipo.setNumeracaoAutomatica(true);
        }
        if (tipo.getMovimentaStock() == null) {
            tipo.setMovimentaStock(false);
        }
        if (tipo.getAfetaContas() == null) {
            tipo.setAfetaContas(false);
        }
        return super.save(tipo);
    }

    // ==================== MÉTODOS COM RETORNO DTO ====================

    public Page<DocumentoTipoDTO> findAllDTO(Pageable pageable) {
        Page<DocumentoTipo> tipos = findAll(pageable);
        return tipos.map(DocumentoTipoDTO::fromEntity);
    }

    public DocumentoTipoDTO findByIdDTO(Long id) {
        DocumentoTipo tipo = findByIdOrThrow(id);
        return DocumentoTipoDTO.fromEntity(tipo);
    }

    public List<DocumentoTipoDTO> findByClasseDTO(DocumentoTipo.ClasseDocumento classe) {
        List<DocumentoTipo> tipos = documentoTipoRepository.findByClasse(classe);
        return tipos.stream()
                .map(DocumentoTipoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DocumentoTipoDTO> findTiposQueMovimentamStockDTO() {
        List<DocumentoTipo> tipos = documentoTipoRepository.findByMovimentaStockTrue();
        return tipos.stream()
                .map(DocumentoTipoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DocumentoTipoDTO> findTiposQueAfetamContasDTO() {
        List<DocumentoTipo> tipos = documentoTipoRepository.findByAfetaContasTrue();
        return tipos.stream()
                .map(DocumentoTipoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS ORIGINAIS (MANTIDOS) ====================

    public List<DocumentoTipo> findByClasse(DocumentoTipo.ClasseDocumento classe) {
        return documentoTipoRepository.findByClasse(classe);
    }

    public List<DocumentoTipo> findTiposQueMovimentamStock() {
        return documentoTipoRepository.findByMovimentaStockTrue();
    }

    public List<DocumentoTipo> findTiposQueAfetamContas() {
        return documentoTipoRepository.findByAfetaContasTrue();
    }

    @Transactional
    public DocumentoTipo criarTipoPadrao(String descricao, DocumentoTipo.ClasseDocumento classe,
                                         String prefixo, Boolean movimentaStock, Boolean afetaContas) {
        DocumentoTipo tipo = new DocumentoTipo();
        tipo.setDescricao(descricao);
        tipo.setClasse(classe);
        tipo.setSeriePrefixo(prefixo);
        tipo.setMovimentaStock(movimentaStock != null ? movimentaStock : false);
        tipo.setAfetaContas(afetaContas != null ? afetaContas : false);
        tipo.setNumeracaoAutomatica(true);
        return save(tipo);
    }

    @Transactional
    public void inicializarTiposDocumento() {
        if (documentoTipoRepository.count() == 0) {
            criarTipoPadrao("Fatura", DocumentoTipo.ClasseDocumento.VENDA, "FAT", true, true);
            criarTipoPadrao("Fatura Proforma", DocumentoTipo.ClasseDocumento.VENDA, "FP", false, false);
            criarTipoPadrao("Nota de Crédito", DocumentoTipo.ClasseDocumento.VENDA, "NC", true, true);
            criarTipoPadrao("Nota de Débito", DocumentoTipo.ClasseDocumento.VENDA, "ND", true, true);
            criarTipoPadrao("Recibo", DocumentoTipo.ClasseDocumento.VENDA, "REC", false, true);
            criarTipoPadrao("Orçamento", DocumentoTipo.ClasseDocumento.VENDA, "ORC", false, false);
            criarTipoPadrao("Guia de Transporte", DocumentoTipo.ClasseDocumento.VENDA, "GT", false, false);
            criarTipoPadrao("Compra", DocumentoTipo.ClasseDocumento.COMPRA, "COM", true, true);
            criarTipoPadrao("Pagamento", DocumentoTipo.ClasseDocumento.FINANCEIRO, "PAG", false, true);
            criarTipoPadrao("Recebimento", DocumentoTipo.ClasseDocumento.FINANCEIRO, "REC", false, true);
            log.info("Default document types initialized");
        }
    }
}