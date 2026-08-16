package reset.reset.Services.financial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.financial.Conta;
import reset.reset.Models.financial.MovimentoConta;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.financial.ContaRepository;
import reset.reset.Repositories.financial.MovimentoContaRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class ContaService extends BaseServiceImpl<Conta, Long, ContaRepository> {

    private final ContaRepository contaRepository;
    @Autowired
    private MovimentoContaRepository movimentoContaRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public ContaService(ContaRepository repository) {
        super(repository);
        this.contaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Conta conta) {
        validateEmpresaExists(conta.getEmpresa().getId());
        validateTipoConta(conta.getTipo());
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateTipoConta(String tipo) {
        if (tipo == null || tipo.isEmpty()) {
            throw new BusinessException("Account type is required");
        }
        List<String> tiposValidos = List.of("CAIXA", "BANCO");
        if (!tiposValidos.contains(tipo)) {
            throw new BusinessException("Invalid account type. Valid types: " + tiposValidos);
        }
    }

    @Transactional
    public MovimentoConta registrarMovimento(Long contaId, String tipo, BigDecimal valor,
                                             Long documentoId, LocalDate data) {
        Conta conta = findByIdOrThrow(contaId);

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Value must be greater than zero");
        }

        if (!"ENTRADA".equals(tipo) && !"SAIDA".equals(tipo)) {
            throw new BusinessException("Invalid movement type. Valid types: ENTRADA, SAIDA");
        }

        MovimentoConta movimento = new MovimentoConta();
        movimento.setConta(conta);
        movimento.setTipo(tipo);
        movimento.setValor(valor);
        movimento.setData(data != null ? data : LocalDate.now());

        return movimentoContaRepository.save(movimento);
    }

    public BigDecimal getSaldoConta(Long contaId) {
        Conta conta = findByIdOrThrow(contaId);

        BigDecimal totalEntradas = movimentoContaRepository.sumByContaIdAndTipo(contaId, "ENTRADA");
        BigDecimal totalSaidas = movimentoContaRepository.sumByContaIdAndTipo(contaId, "SAIDA");

        totalEntradas = totalEntradas != null ? totalEntradas : BigDecimal.ZERO;
        totalSaidas = totalSaidas != null ? totalSaidas : BigDecimal.ZERO;

        return totalEntradas.subtract(totalSaidas);
    }

    public Page<Conta> findByEmpresaId(Long empresaId, Pageable pageable) {
        return contaRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<Conta> findByTipo(String tipo) {
        return contaRepository.findByTipo(tipo);
    }
}

