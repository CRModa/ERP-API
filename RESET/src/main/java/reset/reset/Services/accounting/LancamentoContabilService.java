package reset.reset.Services.accounting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.accounting.Diario;
import reset.reset.Models.accounting.LancamentoContabil;
import reset.reset.Models.accounting.LancamentoContabilLinha;
import reset.reset.Models.auth.User;
import reset.reset.Models.document.Documento;
import reset.reset.Repositories.accounting.ContaContabilRepository;
import reset.reset.Repositories.accounting.DiarioRepository;
import reset.reset.Repositories.accounting.LancamentoContabilLinhaRepository;
import reset.reset.Repositories.accounting.LancamentoContabilRepository;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class LancamentoContabilService extends BaseServiceImpl<LancamentoContabil, Long, LancamentoContabilRepository> {

    private final LancamentoContabilRepository lancamentoRepository;
    @Autowired
    private LancamentoContabilLinhaRepository linhaRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private DiarioRepository diarioRepository;
    @Autowired
    private ContaContabilRepository contaContabilRepository;
    @Autowired
    private DocumentoRepository documentoRepository;
    @Autowired
    private UserRepository utilizadorRepository;

    public LancamentoContabilService(LancamentoContabilRepository repository) {
        super(repository);
        this.lancamentoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(LancamentoContabil lancamento) {
        validateEmpresaExists(lancamento.getEmpresa().getId());
        validateDiarioExists(lancamento.getDiario().getId());
        validateDocumento(lancamento.getDocumento());
        validateUtilizador(lancamento.getUtilizador());
        validateLancamentoLinhas(lancamento.getLinhas());
        validateLancamentoBalanceado(lancamento.getLinhas());
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateDiarioExists(Long diarioId) {
        if (!diarioRepository.existsById(diarioId)) {
            throw new EntityNotFoundException("Journal not found with id: " + diarioId);
        }
    }

    private void validateDocumento(Documento documento) {
        if (documento != null && !documentoRepository.existsById(documento.getId())) {
            throw new EntityNotFoundException("Document not found with id: " + documento.getId());
        }
    }

    private void validateUtilizador(User utilizador) {
        if (utilizador != null && !utilizadorRepository.existsById(utilizador.getId())) {
            throw new EntityNotFoundException("User not found with id: " + utilizador.getId());
        }
    }

    private void validateLancamentoLinhas(List<LancamentoContabilLinha> linhas) {
        if (linhas == null || linhas.isEmpty()) {
            throw new BusinessException("Journal entry must have at least one line");
        }

        for (LancamentoContabilLinha linha : linhas) {
            if (linha.getValor() == null || linha.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Line value must be greater than zero");
            }
            if (linha.getNatureza() == null) {
                throw new BusinessException("Line nature (D/C) is required");
            }
            validateContaContabil(linha.getContaContabil().getId());
        }
    }

    private void validateContaContabil(Long contaId) {
        if (!contaContabilRepository.existsById(contaId)) {
            throw new EntityNotFoundException("Account not found with id: " + contaId);
        }
    }

    private void validateLancamentoBalanceado(List<LancamentoContabilLinha> linhas) {
        BigDecimal totalDebito = linhas.stream()
                .filter(l -> l.getNatureza() == LancamentoContabilLinha.Natureza.D)
                .map(LancamentoContabilLinha::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredito = linhas.stream()
                .filter(l -> l.getNatureza() == LancamentoContabilLinha.Natureza.C)
                .map(LancamentoContabilLinha::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebito.compareTo(totalCredito) != 0) {
            throw new BusinessException(
                    "Journal entry is not balanced. Debit: " + totalDebito + ", Credit: " + totalCredito
            );
        }
    }

    @Override
    @Transactional
    public LancamentoContabil save(LancamentoContabil lancamento) {
        // Generate entry number if not provided
        if (lancamento.getNumeroLancamento() == null || lancamento.getNumeroLancamento().isEmpty()) {
            lancamento.setNumeroLancamento(gerarNumeroLancamento(lancamento));
        }

        // Set default values
        if (lancamento.getDataLancamento() == null) {
            lancamento.setDataLancamento(LocalDate.now());
        }
        if (lancamento.getDataValor() == null) {
            lancamento.setDataValor(lancamento.getDataLancamento());
        }

        LancamentoContabil saved = super.save(lancamento);

        // Save lines with reference to saved lancamento
        if (lancamento.getLinhas() != null) {
            for (LancamentoContabilLinha linha : lancamento.getLinhas()) {
                linha.setLancamento(saved);
                linhaRepository.save(linha);
            }
        }

        return saved;
    }

    private String gerarNumeroLancamento(LancamentoContabil lancamento) {
        String ano = String.valueOf(LocalDate.now().getYear());
        String empresaId = String.format("%03d", lancamento.getEmpresa().getId());
        String sequencia = gerarSequenciaLancamento(lancamento.getEmpresa().getId());
        return "LANC-" + empresaId + "-" + ano + "-" + sequencia;
    }

    private String gerarSequenciaLancamento(Long empresaId) {
        String maxNumero = lancamentoRepository.findMaxNumeroLancamentoByEmpresaId(empresaId);
        if (maxNumero == null) {
            return "000001";
        }

        String[] parts = maxNumero.split("-");
        String seq = parts[parts.length - 1];
        int num = Integer.parseInt(seq) + 1;
        return String.format("%06d", num);
    }

    @Transactional
    public LancamentoContabil criarLancamentoManual(Long empresaId, Long diarioId, String descricao,
                                                    List<LancamentoContabilLinha> linhas) {
        LancamentoContabil lancamento = new LancamentoContabil();
        lancamento.setEmpresa(empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa not found")));
        lancamento.setDiario(diarioRepository.findById(diarioId)
                .orElseThrow(() -> new EntityNotFoundException("Journal not found")));
        lancamento.setDescricao(descricao);
        lancamento.setLinhas(linhas);

        return save(lancamento);
    }

    @Transactional
    public LancamentoContabil criarLancamentoFromDocumento(Long documentoId, String descricao,
                                                           List<LancamentoContabilLinha> linhas) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        LancamentoContabil lancamento = new LancamentoContabil();
        lancamento.setEmpresa(documento.getEmpresa());
        lancamento.setDocumento(documento);
        lancamento.setDescricao(descricao);
        lancamento.setLinhas(linhas);

        // Use default diario
        Diario diario = diarioRepository.findByCodigo("DIARIO_GERAL")
                .orElseThrow(() -> new BusinessException("Default journal not found"));
        lancamento.setDiario(diario);

        return save(lancamento);
    }

    public Page<LancamentoContabil> findByEmpresaId(Long empresaId, Pageable pageable) {
        return lancamentoRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<LancamentoContabil> findByDiarioId(Long diarioId) {
        return lancamentoRepository.findByDiarioId(diarioId);
    }

    public List<LancamentoContabil> findByDocumentoId(Long documentoId) {
        return lancamentoRepository.findByDocumentoId(documentoId);
    }

    public List<LancamentoContabil> findByDataLancamentoBetween(LocalDate inicio, LocalDate fim) {
        return lancamentoRepository.findByDataLancamentoBetween(inicio, fim);
    }

    public LancamentoContabil findByNumeroLancamento(String numeroLancamento) {
        return lancamentoRepository.findByNumeroLancamento(numeroLancamento)
                .orElseThrow(() -> new EntityNotFoundException("Journal entry not found: " + numeroLancamento));
    }
}

