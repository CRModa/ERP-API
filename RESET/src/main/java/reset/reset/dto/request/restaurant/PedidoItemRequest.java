package reset.reset.dto.request.restaurant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import reset.reset.Models.product.Desconto;
import reset.reset.Models.product.Iva;
import reset.reset.Models.product.Produto;
import reset.reset.Models.restaurant.Combo;
import reset.reset.Models.restaurant.ItemPedido;

import java.math.BigDecimal;

@Data
public class PedidoItemRequest {

    private Long itemId;
    private Long comboId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade = BigDecimal.ONE;

    private Long descontoId;
    private Long ivaId;
    private BigDecimal descontoValor;
    private String observacao;

    public ItemPedido toEntity() {
        ItemPedido item = new ItemPedido();
        item.setQuantidade(this.quantidade);
        item.setDescontoValor(this.descontoValor);
        item.setObservacao(this.observacao);

        if (this.itemId != null) {
            Produto itemCardapio = new Produto();
            itemCardapio.setId(this.itemId);
            item.setProduto(itemCardapio);
        }

        if (this.comboId != null) {
            Combo combo = new Combo();
            combo.setId(this.comboId);
            item.setCombo(combo);
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
