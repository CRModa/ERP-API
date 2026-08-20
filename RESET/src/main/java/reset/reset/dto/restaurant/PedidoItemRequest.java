package reset.reset.dto.restaurant;

import lombok.Data;
import reset.reset.Models.product.Produto;
import reset.reset.Models.restaurant.ItemPedido;

import java.math.BigDecimal;

@Data
public class PedidoItemRequest {

//    @NotNull(message = "Produto ID é obrigatório")
    private Long produtoId;

//    @NotNull(message = "Quantidade é obrigatória")
//    @Positive(message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade = BigDecimal.ONE;

    private Long descontoId;
    private Long ivaId;
    private BigDecimal descontoValor;
    private String observacao;
    private Boolean isCombo = false;
    private Long comboId;

    public ItemPedido toEntity() {
        ItemPedido item = new ItemPedido();
        item.setQuantidade(this.quantidade);
        item.setDescontoValor(this.descontoValor);
        item.setObservacao(this.observacao);

        if (this.produtoId != null) {
            Produto produto = new Produto();
            produto.setId(this.produtoId);
            item.setProduto(produto);
        }

        return item;
    }
}
