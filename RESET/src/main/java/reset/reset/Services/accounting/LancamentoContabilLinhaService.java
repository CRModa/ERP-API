package reset.reset.Services.accounting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.accounting.LancamentoContabilLinha;
import reset.reset.Repositories.accounting.ContaContabilRepository;
import reset.reset.Repositories.accounting.LancamentoContabilLinhaRepository;
import reset.reset.Repositories.accounting.LancamentoContabilRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class LancamentoContabilLinhaService extends BaseServiceImpl<LancamentoContabilLinha, Long, LancamentoContabilLinhaRepository> {

    private final LancamentoContabilLinhaRepository linhaRepository;
    @Autowired
    private LancamentoContabilRepository lancamentoRepository;
    @Autowired
    private ContaContabilRepository contaContabilRepository;

    public LancamentoContabilLinhaService(LancamentoContabilLinhaRepository repository) {
        super(repository);
        this.linhaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(LancamentoContabilLinha linha) {
        if (linha.getValor() == null || linha.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Line value must be greater than zero");
        }
        if (linha.getNatureza() == null) {
            throw new BusinessException("Line nature (D/C) is required");
        }
        if (linha.getLancamento() == null || !lancamentoRepository.existsById(linha.getLancamento().getId())) {
            throw new EntityNotFoundException("Journal entry not found");
        }
        if (linha.getContaContabil() == null || !contaContabilRepository.existsById(linha.getContaContabil().getId())) {
            throw new EntityNotFoundException("Account not found");
        }
    }

    public List<LancamentoContabilLinha> findByLancamentoId(Long lancamentoId) {
        return linhaRepository.findByLancamentoId(lancamentoId);
    }

    public List<LancamentoContabilLinha> findByContaContabilId(Long contaId) {
        return linhaRepository.findByContaContabilId(contaId);
    }

    public BigDecimal sumDebitosByLancamentoId(Long lancamentoId) {
        BigDecimal sum = linhaRepository.sumDebitosByLancamentoId(lancamentoId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public BigDecimal sumCreditosByLancamentoId(Long lancamentoId) {
        BigDecimal sum = linhaRepository.sumCreditosByLancamentoId(lancamentoId);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
