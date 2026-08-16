package reset.reset.dto.request;

import lombok.Data;
import reset.reset.Models.product.Iva;
import reset.reset.Models.product.Produto;
import reset.reset.Models.purchase.CompraItem;

import java.math.BigDecimal;

@Data
public class CompraItemRequest {
    private Long produtoId;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private Long ivaId;

    public CompraItem toEntity() {
        CompraItem item = new CompraItem();
        item.setQuantidade(this.quantidade);
        item.setPrecoUnitario(this.precoUnitario);

        if (this.produtoId != null) {
            Produto produto = new Produto();
            produto.setId(this.produtoId);
            item.setProduto(produto);
        }

        if (this.ivaId != null) {
            Iva iva = new Iva();
            iva.setId(this.ivaId);
            item.setIva(iva);
        }

        return item;
    }
}
