package reset.reset.dto.request.restaurant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import reset.reset.Models.restaurant.ItemPedido;

import java.math.BigDecimal;

@Data
public class AdicionarItemPedidoRequest {

    @NotNull(message = "Item ID é obrigatório")
    private Long itemId;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade = BigDecimal.ONE;

    private Long descontoId;
    private BigDecimal descontoValor;
    private String observacao;

    public ItemPedido toEntity() {
        ItemPedido item = new ItemPedido();
        item.setQuantidade(this.quantidade);
        item.setDescontoValor(this.descontoValor);
        item.setObservacao(this.observacao);
        item.setId(this.itemId);
        item.setObservacao(this.observacao);

        return item;
    }
}
