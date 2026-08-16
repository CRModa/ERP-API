package reset.reset.Repositories.purchase;

import org.springframework.stereotype.Repository;
import reset.reset.Models.purchase.CompraItem;
import reset.reset.Repositories.BaseRepository;

import java.util.List;

@Repository
public interface CompraItemRepository extends BaseRepository<CompraItem, Long> {
    List<CompraItem> findByCompraId(Long compraId);

    List<CompraItem> findByProdutoId(Long produtoId);
}