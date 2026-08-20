package reset.reset.Repositories.product;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reset.reset.Models.product.ProdutoCompostoItem;
import reset.reset.Repositories.BaseRepository;

import java.util.List;

public interface ProdutoCompostoItemRepository extends BaseRepository<ProdutoCompostoItem, Long> {

    @Query("SELECT i FROM ProdutoCompostoItem i WHERE i.produtoPai.id = :produtoPaiId")
    List<ProdutoCompostoItem> findByProdutoPaiId(@Param("produtoPaiId") Long produtoPaiId);

    @Query("SELECT i FROM ProdutoCompostoItem i WHERE i.produtoFilho.id = :produtoFilhoId")
    List<ProdutoCompostoItem> findByProdutoFilhoId(@Param("produtoFilhoId") Long produtoFilhoId);

    @Query("SELECT COUNT(i) FROM ProdutoCompostoItem i WHERE i.produtoFilho.id = :produtoFilhoId")
    long countByProdutoFilhoId(@Param("produtoFilhoId") Long produtoFilhoId);
}
