package reset.reset.Services.financial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.User;
import reset.reset.Models.financial.Conta;
import reset.reset.Models.financial.MovimentoConta;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.financial.ContaRepository;
import reset.reset.Repositories.financial.MovimentoContaRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.financial.ContaDTO;
import reset.reset.dto.financial.MovimentoContaDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContaService extends BaseServiceImpl<Conta, Long, ContaRepository> {

    private final ContaRepository contaRepository;

    @Autowired
    private MovimentoContaRepository movimentoContaRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private UserRepository userRepository;

    public ContaService(ContaRepository repository) {
        super(repository);
        this.contaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Conta conta) {
//        validateEmpresaExists(conta.getEmpresa().getId());
        conta.setEmpresa(getAuthenticatedUser().getEmpresa());
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

    // ==================== MÉTODOS COM RETORNO DTO ====================

    public Page<ContaDTO> findAllDTO(Pageable pageable) {
        Page<Conta> contas = findAll(pageable);

        return contas.map(conta -> {
            BigDecimal saldo = getSaldoConta(conta.getId());
            ContaDTO contaDTO = ContaDTO.fromEntity(conta);
            contaDTO.setSaldo(saldo);
            return contaDTO;
        });
    }

    public ContaDTO findByIdDTO(Long id) {
        Conta conta = findByIdOrThrow(id);
        return ContaDTO.fromEntity(conta);
    }

    public Page<ContaDTO> findByEmpresaIdDTO(Long empresaId, Pageable pageable) {
        Page<Conta> contas = contaRepository.findByEmpresaId(empresaId, pageable);
        return contas.map(ContaDTO::fromEntity);
    }

    public List<ContaDTO> findByTipoDTO(String tipo) {
        List<Conta> contas = contaRepository.findByTipo(tipo);
        return contas.stream()
                .map(ContaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ContaDTO getSaldoContaComMovimentosDTO(Long contaId) {
        Conta conta = findByIdOrThrow(contaId);
        ContaDTO dto = ContaDTO.fromEntity(conta);
        dto.setSaldo(getSaldoConta(contaId));
        return dto;
    }

    @Transactional
    public MovimentoContaDTO registrarMovimentoDTO(Long contaId, String tipo, BigDecimal valor,
                                                   Long documentoId, LocalDate data) {
        MovimentoConta movimento = registrarMovimento(contaId, tipo, valor, documentoId, data);
        return MovimentoContaDTO.fromEntity(movimento);
    }

    public Page<MovimentoContaDTO> findMovimentosByContaIdDTO(Long contaId, Pageable pageable) {
        Page<MovimentoConta> movimentos = movimentoContaRepository.findByContaId(contaId, pageable);
        return movimentos.map(MovimentoContaDTO::fromEntity);
    }

    // ==================== MÉTODOS ORIGINAIS (MANTIDOS) ====================

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

    @Transactional
    public ContaDTO ativarConta(Long id) {
        Conta conta = findByIdOrThrow(id);

        // Verifica se a conta já está ativa
        if (Boolean.TRUE.equals(conta.getAtivo())) {
            throw new BusinessException("Account is already active");
        }

        conta.setAtivo(true);
        Conta updated = repository.save(conta);
        return ContaDTO.fromEntity(updated);
    }

    @Transactional
    public ContaDTO desativarConta(Long id) {
        Conta conta = findByIdOrThrow(id);

        // Verifica se a conta já está desativada
        if (Boolean.FALSE.equals(conta.getAtivo())) {
            throw new BusinessException("Account is already inactive");
        }

        // Verifica se a conta possui saldo antes de desativar
        BigDecimal saldo = getSaldoConta(id);
        if (saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Cannot deactivate account with non-zero balance. Current balance: " + saldo);
        }

        //implementacao de verificacao se a conta tem movimentos pendentes

        conta.setAtivo(false);
        Conta updated = repository.save(conta);
        return ContaDTO.fromEntity(updated);
    }

    private User getAuthenticatedUser() {
        try {
            UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userRepository.findById(principal.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
        } catch (Exception e) {
            throw new BusinessException("User not authenticated");
        }
    }
}