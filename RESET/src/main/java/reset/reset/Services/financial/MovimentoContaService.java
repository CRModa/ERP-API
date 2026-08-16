package reset.reset.Services.financial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.financial.MovimentoConta;
import reset.reset.Repositories.financial.ContaRepository;
import reset.reset.Repositories.financial.MovimentoContaRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class MovimentoContaService extends BaseServiceImpl<MovimentoConta, Long, MovimentoContaRepository> {

    private final MovimentoContaRepository movimentoContaRepository;
    @Autowired
    private ContaRepository contaRepository;

    public MovimentoContaService(MovimentoContaRepository repository) {
        super(repository);
        this.movimentoContaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(MovimentoConta movimento) {
        if (movimento.getConta() == null || !contaRepository.existsById(movimento.getConta().getId())) {
            throw new EntityNotFoundException("Account not found");
        }
        if (movimento.getValor() == null || movimento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Value must be greater than zero");
        }
        if (!"ENTRADA".equals(movimento.getTipo()) && !"SAIDA".equals(movimento.getTipo())) {
            throw new BusinessException("Invalid movement type. Valid types: ENTRADA, SAIDA");
        }
    }

    public Page<MovimentoConta> findByContaId(Long contaId, Pageable pageable) {
        return movimentoContaRepository.findByContaId(contaId, pageable);
    }

    public List<MovimentoConta> findByDocumentoId(Long documentoId) {
        return movimentoContaRepository.findByDocumentoId(documentoId);
    }

    public List<MovimentoConta> findByDataBetween(LocalDate inicio, LocalDate fim) {
        return movimentoContaRepository.findByDataBetween(inicio, fim);
    }

    public BigDecimal sumByContaIdAndTipo(Long contaId, String tipo) {
        BigDecimal sum = movimentoContaRepository.sumByContaIdAndTipo(contaId, tipo);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
