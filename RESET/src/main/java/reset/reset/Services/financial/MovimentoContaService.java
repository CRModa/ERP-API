package reset.reset.Services.financial;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.User;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.financial.MovimentoConta;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.financial.ContaRepository;
import reset.reset.Repositories.financial.MovimentoContaRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.financial.MovimentoContaDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovimentoContaService extends BaseServiceImpl<MovimentoConta, Long, MovimentoContaRepository> {

    private final MovimentoContaRepository movimentoContaRepository;

    @Autowired
    private ContaRepository contaRepository;
    @Autowired
    private UserRepository userRepository;

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

    // ==================== MÉTODOS COM RETORNO DTO ====================

    public MovimentoContaDTO findByIdDTO(Long id) {
        MovimentoConta movimento = findByIdOrThrow(id);
        return MovimentoContaDTO.fromEntity(movimento);
    }

    public Page<MovimentoContaDTO> findAllDTO(Pageable pageable) {
        Page<MovimentoConta> movimentos = findAll(pageable);
        return movimentos.map(MovimentoContaDTO::fromEntity);
    }

    public Page<MovimentoContaDTO> findByContaIdDTO(Long contaId, Pageable pageable) {
        Page<MovimentoConta> movimentos = movimentoContaRepository.findByContaId(contaId, pageable);
        return movimentos.map(MovimentoContaDTO::fromEntity);
    }

    public List<MovimentoContaDTO> findByDocumentoIdDTO(Long documentoId) {
        List<MovimentoConta> movimentos = movimentoContaRepository.findByDocumentoId(documentoId);
        return movimentos.stream()
                .map(MovimentoContaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MovimentoContaDTO> findByDataBetweenDTO(LocalDate inicio, LocalDate fim) {
        List<MovimentoConta> movimentos = movimentoContaRepository.findByDataBetween(inicio, fim);
        return movimentos.stream()
                .map(MovimentoContaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MovimentoContaDTO> findByFiltrosDTO(Long contaId, String tipo, LocalDate dataInicio, LocalDate dataFim) {
        List<MovimentoConta> movimentos = movimentoContaRepository.findByFiltros(contaId, tipo, dataInicio, dataFim);
        return movimentos.stream()
                .map(MovimentoContaDTO::fromEntity)
                .collect(Collectors.toList());
    }

//    public Page<MovimentoContaDTO> findByFiltrosPaginadoDTO(Long contaId, String tipo, LocalDate dataInicio,
//                                                            LocalDate dataFim, Pageable pageable) {
//        Page<MovimentoConta> movimentos = movimentoContaRepository.findByFiltrosPaginado(
//                contaId, tipo, dataInicio, dataFim, pageable);
//        return movimentos.map(MovimentoContaDTO::fromEntity);
//    }

    public Page<MovimentoContaDTO> findByFiltrosPaginadoDTO(Long contaId, String tipo,
                                                            LocalDate dataInicio,
                                                            LocalDate dataFim,
                                                            Pageable pageable) {
        Specification<MovimentoConta> spec = buildSpecification(contaId, tipo, dataInicio, dataFim);
        Page<MovimentoConta> movimentos = movimentoContaRepository.findAll(spec, pageable);
        return movimentos.map(MovimentoContaDTO::fromEntity);
    }

    private Specification<MovimentoConta> buildSpecification(Long contaId, String tipo,
                                                             LocalDate dataInicio,
                                                             LocalDate dataFim) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Empresa empresa = getAuthenticatedUser().getEmpresa();

            predicates.add(cb.equal(root.get("conta").get("empresa"), empresa));

            if (contaId != null) {
                predicates.add(cb.equal(root.get("conta").get("id"), contaId));
            }

            if (tipo != null && !tipo.isEmpty()) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }

            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("data"), dataInicio));
            }

            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("data"), dataFim));
            }

            // Se não houver filtros, retorna sempre verdadeiro (todos os registros)
            if (predicates.isEmpty()) {
                return cb.isTrue(cb.literal(true));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public BigDecimal sumByContaIdAndTipoDTO(Long contaId, String tipo) {
        BigDecimal sum = movimentoContaRepository.sumByContaIdAndTipo(contaId, tipo);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    // ==================== MÉTODOS ORIGINAIS (MANTIDOS) ====================

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