package reset.reset.Services.purchase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.purchase.CompraItem;
import reset.reset.Repositories.purchase.CompraItemRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class CompraItemService extends BaseServiceImpl<CompraItem, Long, CompraItemRepository> {

    private final CompraItemRepository compraItemRepository;

    public CompraItemService(CompraItemRepository repository) {
        super(repository);
        this.compraItemRepository = repository;
    }

    @Override
    protected void validateBeforeSave(CompraItem item) {
        if (item.getQuantidade() == null || item.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
        if (item.getPrecoUnitario() == null || item.getPrecoUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Unit price cannot be negative");
        }
    }

    public List<CompraItem> findByCompraId(Long compraId) {
        return compraItemRepository.findByCompraId(compraId);
    }

    public List<CompraItem> findByProdutoId(Long produtoId) {
        return compraItemRepository.findByProdutoId(produtoId);
    }
}
