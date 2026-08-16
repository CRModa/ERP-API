package reset.reset.dto.request;

import lombok.Data;
import reset.reset.Models.document.DocumentoItem;
import reset.reset.Models.product.Desconto;
import reset.reset.Models.product.Iva;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;

@Data
public class DocumentoItemRequest {
    private Long produtoId;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private Long descontoId;
    private Long ivaId;

    public DocumentoItem toEntity() {
        DocumentoItem item = new DocumentoItem();
        item.setQuantidade(this.quantidade);
        item.setPrecoUnitario(this.precoUnitario);

        if (this.produtoId != null) {
            Produto produto = new Produto();
            produto.setId(this.produtoId);
            item.setProduto(produto);
        }

        if (this.descontoId != null) {
            Desconto desconto = new Desconto();
            desconto.setId(this.descontoId);
            item.setDesconto(desconto);
        }

        if (this.ivaId != null) {
            Iva iva = new Iva();
            iva.setId(this.ivaId);
            item.setIva(iva);
        }

        return item;
    }
}
