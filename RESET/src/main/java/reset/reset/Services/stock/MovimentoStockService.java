package reset.reset.Services.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.stock.MovimentoStock;
import reset.reset.Repositories.stock.MovimentoStockRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.MovimentoStockFilter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MovimentoStockService extends BaseServiceImpl<MovimentoStock, Long, MovimentoStockRepository> {

    private final MovimentoStockRepository movimentoStockRepository;

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
}
