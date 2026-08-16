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
import reset.reset.Models.document.DocumentoItem;
import reset.reset.Models.document.DocumentoTipo;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.customer.ClienteRepository;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Repositories.document.DocumentoTipoRepository;
import reset.reset.Repositories.product.ProdutoRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.DocumentoFilter;
import reset.reset.dto.projection.DocumentoResumo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class DocumentoService extends BaseServiceImpl<Documento, Long, DocumentoRepository> {

    private final DocumentoRepository documentoRepository;
    @Autowired
    private DocumentoTipoRepository documentoTipoRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired

    public DocumentoService(DocumentoRepository repository) {
        super(repository);
        this.documentoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Documento documento) {
        validateEmpresaExists(documento.getEmpresa().getId());
        validateClienteExists(documento.getCliente().getId());
        validateDocumentoTipoExists(documento.getTipo().getId());
        validateDocumentoItens(documento.getItens());
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateClienteExists(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new EntityNotFoundException("Cliente not found with id: " + clienteId);
        }
    }

    private void validateDocumentoTipoExists(Long tipoId) {
        if (!documentoTipoRepository.existsById(tipoId)) {
            throw new EntityNotFoundException("Document type not found with id: " + tipoId);
        }
    }

    private void validateDocumentoItens(List<DocumentoItem> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new BusinessException("Document must have at least one item");
        }

        for (DocumentoItem item : itens) {
            if (item.getQuantidade() == null || item.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Item quantity must be greater than zero");
            }
            if (item.getPrecoUnitario() == null || item.getPrecoUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Item price cannot be negative");
            }
            validateProdutoExists(item.getProduto().getId());
        }
    }

    private void validateProdutoExists(Long produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new EntityNotFoundException("Product not found with id: " + produtoId);
        }
    }

    @Override
    @Transactional
    public Documento save(Documento documento) {
        // Generate document number if automatic
        if (documento.getTipo().getNumeracaoAutomatica() && documento.getNumero() == null) {
            documento.setNumero(gerarNumeroDocumento(documento));
        }

        // Calculate total
        BigDecimal total = calcularTotal(documento);
        documento.setTotal(total);

        // Set data if not provided
        if (documento.getData() == null) {
            documento.setData(LocalDate.now());
        }

        // Set estado if not provided
        if (documento.getEstado() == null) {
            documento.setEstado("PENDENTE");
        }

        return super.save(documento);
    }

    private String gerarNumeroDocumento(Documento documento) {
        DocumentoTipo tipo = documento.getTipo();
        String prefixo = tipo.getSeriePrefixo() != null ? tipo.getSeriePrefixo() : "DOC";
        String ano = String.valueOf(LocalDate.now().getYear());
        String mes = String.format("%02d", LocalDate.now().getMonthValue());

        // Get next sequence number
        long count = documentoRepository.count() + 1;
        String seq = String.format("%06d", count);

        return prefixo + ano + mes + seq;
    }

    private BigDecimal calcularTotal(Documento documento) {
        if (documento.getItens() == null || documento.getItens().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return documento.getItens().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getPrecoUnitario().multiply(item.getQuantidade());
                    // Apply discount if any
                    if (item.getDesconto() != null && item.getDesconto().getValor() != null) {
                        if ("PERCENTAGEM".equals(item.getDesconto().getTipo())) {
                            BigDecimal descontoValor = subtotal.multiply(
                                    item.getDesconto().getValor().divide(new BigDecimal("100"))
                            );
                            subtotal = subtotal.subtract(descontoValor);
                        } else {
                            subtotal = subtotal.subtract(item.getDesconto().getValor());
                        }
                    }
                    // Apply IVA if any
                    if (item.getIva() != null && item.getIva().getTaxa() != null) {
                        BigDecimal ivaValor = subtotal.multiply(
                                item.getIva().getTaxa().divide(new BigDecimal("100"))
                        );
                        subtotal = subtotal.add(ivaValor);
                    }
                    return subtotal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Documento mudarEstado(Long documentoId, String novoEstado) {
        Documento documento = findByIdOrThrow(documentoId);
        documento.setEstado(novoEstado);
        return documentoRepository.save(documento);
    }

    public Page<Documento> filter(DocumentoFilter filter) {
        return documentoRepository.filter(filter);
    }

    public Page<Documento> findByClienteId(Long clienteId, Pageable pageable) {
        return documentoRepository.findByClienteId(clienteId, pageable);
    }

    public Page<DocumentoResumo> findDocumentoResumoByEmpresaId(Long empresaId, Pageable pageable) {
        return documentoRepository.findDocumentoResumoByEmpresaId(empresaId, pageable);
    }

    public BigDecimal sumTotalByEmpresaAndPeriodo(Long empresaId, LocalDate inicio, LocalDate fim) {
        BigDecimal sum = documentoRepository.sumTotalByEmpresaAndPeriodo(empresaId, inicio, fim);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public long countByEmpresaAndPeriodo(Long empresaId, LocalDate inicio, LocalDate fim) {
        return documentoRepository.countByEmpresaAndPeriodo(empresaId, inicio, fim);
    }
}
