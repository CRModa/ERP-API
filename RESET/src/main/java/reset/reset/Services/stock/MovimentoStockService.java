package reset.reset.Services.stock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.User;
import reset.reset.Models.stock.MovimentoStock;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.stock.MovimentoStockRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.MovimentoStockFilter;
import reset.reset.dto.stock.MovimentoStockDTO;
import reset.reset.dto.stock.MovimentoStockResumoDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovimentoStockService extends BaseServiceImpl<MovimentoStock, Long, MovimentoStockRepository> {

    private final MovimentoStockRepository movimentoStockRepository;
    @Autowired
    private UserRepository userRepository;

    public MovimentoStockService(MovimentoStockRepository repository) {
        super(repository);
        this.movimentoStockRepository = repository;
    }

    @Override
    protected void validateBeforeSave(MovimentoStock movimento) {
        if (movimento.getQuantidade() == null || movimento.getQuantidade().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("Quantity must be different from zero");
        }
        if (movimento.getTipo() == null || movimento.getTipo().isEmpty()) {
            throw new BusinessException("Movement type is required");
        }
    }

    // ==================== MÉTODOS COM RETORNO DTO ====================

    public Page<MovimentoStockDTO> filterDTO(MovimentoStockFilter filter) {
        filter.setEmpresaId(getCurrentEmpresaId());
        Page<MovimentoStock> movimentos = movimentoStockRepository.filter(filter);
        return movimentos.map(MovimentoStockDTO::fromEntity);
    }

    public Page<MovimentoStockDTO> findByProdutoIdDTO(Long produtoId, Pageable pageable) {
        // Buscar movimentos do produto de forma paginada
        List<MovimentoStock> movimentos = movimentoStockRepository.findByProdutoIdOrderByDataMovimentoDesc(produtoId);

        // Converter para página manualmente (considerando que o repositório já retorna lista)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), movimentos.size());

        List<MovimentoStock> pagedList = movimentos.subList(start, end);
        Page<MovimentoStock> page = new org.springframework.data.domain.PageImpl<>(
                pagedList, pageable, movimentos.size()
        );

        return page.map(MovimentoStockDTO::fromEntity);
    }

    public List<MovimentoStockDTO> findMovimentosByProdutoAndPeriodoDTO(Long produtoId,
                                                                        LocalDateTime inicio,
                                                                        LocalDateTime fim) {
        List<MovimentoStock> movimentos = movimentoStockRepository.findMovimentosByProdutoAndPeriodo(produtoId, inicio, fim);
        return movimentos.stream()
                .map(MovimentoStockDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<MovimentoStockDTO> findByArmazemIdDTO(Long armazemId, Pageable pageable) {
        Page<MovimentoStock> movimentos = movimentoStockRepository.findByArmazemId(armazemId, pageable);
        return movimentos.map(MovimentoStockDTO::fromEntity);
    }

    public Page<MovimentoStockDTO> findByEmpresaIdDTO(Pageable pageable) {
        // Implementação baseada no usuário autenticado
        Long empresaId = getCurrentEmpresaId();
        Page<MovimentoStock> movimentos = movimentoStockRepository.findByEmpresaId(empresaId, pageable);
        return movimentos.map(MovimentoStockDTO::fromEntity);
    }

    public List<MovimentoStockResumoDTO> findByTipoDTO(String tipo) {
        List<MovimentoStock> movimentos = movimentoStockRepository.findByTipo(tipo);
        return movimentos.stream()
                .map(MovimentoStockResumoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MovimentoStockResumoDTO> findByReferenciaDTO(String referencia) {
        List<MovimentoStock> movimentos = movimentoStockRepository.findByReferencia(referencia);
        return movimentos.stream()
                .map(MovimentoStockResumoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MovimentoStockResumoDTO> findResumoByEmpresaDTO(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        Page<MovimentoStock> movimentos = movimentoStockRepository.findByEmpresaId(empresaId, pageable);
        return movimentos.stream()
                .map(MovimentoStockResumoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS ORIGINAIS (MANTIDOS PARA COMPATIBILIDADE) ====================

    public Page<MovimentoStock> filter(MovimentoStockFilter filter) {
        return movimentoStockRepository.filter(filter);
    }

    public List<MovimentoStock> findMovimentosByProdutoAndPeriodo(Long produtoId,
                                                                  LocalDateTime inicio,
                                                                  LocalDateTime fim) {
        return movimentoStockRepository.findMovimentosByProdutoAndPeriodo(produtoId, inicio, fim);
    }

    public BigDecimal sumQuantidadeByProdutoAndTipo(Long produtoId, String tipo) {
        BigDecimal sum = movimentoStockRepository.sumQuantidadeByProdutoAndTipo(produtoId, tipo);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private User getAuthenticatedUser() {
        try {
            UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userRepository.findById(principal.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
        } catch (Exception e) {
            throw new BusinessException("User not authenticated");
        }
    }

    private Long getCurrentEmpresaId() {
        return getAuthenticatedUser().getEmpresa().getId();
    }
}
