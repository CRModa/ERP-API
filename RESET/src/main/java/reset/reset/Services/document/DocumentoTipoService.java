package reset.reset.Services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.DocumentoTipo;
import reset.reset.Repositories.document.DocumentoTipoRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.util.List;

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
        // Check if description already exists (using custom query)
        // For simplicity, we'll assume we need to add this method to repository
    }

    @Override
    @Transactional
    public DocumentoTipo save(DocumentoTipo tipo) {
        // Set default values
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
        // Tipos de Venda
        criarTipoPadrao("Fatura", DocumentoTipo.ClasseDocumento.VENDA, "FAT", true, true);
        criarTipoPadrao("Fatura Proforma", DocumentoTipo.ClasseDocumento.VENDA, "FP", false, false);
        criarTipoPadrao("Nota de Crédito", DocumentoTipo.ClasseDocumento.VENDA, "NC", true, true);
        criarTipoPadrao("Nota de Débito", DocumentoTipo.ClasseDocumento.VENDA, "ND", true, true);
        criarTipoPadrao("Recibo", DocumentoTipo.ClasseDocumento.VENDA, "REC", false, true);
        criarTipoPadrao("Orçamento", DocumentoTipo.ClasseDocumento.VENDA, "ORC", false, false);
        criarTipoPadrao("Guia de Transporte", DocumentoTipo.ClasseDocumento.VENDA, "GT", false, false);

        // Tipos de Compra
        criarTipoPadrao("Compra", DocumentoTipo.ClasseDocumento.COMPRA, "COM", true, true);

        // Tipos Financeiros
        criarTipoPadrao("Pagamento", DocumentoTipo.ClasseDocumento.FINANCEIRO, "PAG", false, true);
        criarTipoPadrao("Recebimento", DocumentoTipo.ClasseDocumento.FINANCEIRO, "REC", false, true);
    }
}
