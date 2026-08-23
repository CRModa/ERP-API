package reset.reset.dto.request.restaurant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.restaurant.ItemPedido;
import reset.reset.Models.restaurant.Pedido;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoRequest {

    @NotNull(message = "ID do produto é obrigatório")
    private Long produtoId;

    @Min(value = 1, message = "Quantidade deve ser maior que 0")
    private BigDecimal quantidade = BigDecimal.ONE;

    private BigDecimal precoUnitario;

    private Long comboId;

    private Long descontoId;

    private String observacao;

    public ItemPedido toEntity(Produto produto, Pedido pedido) {
        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(this.quantidade != null ? this.quantidade : BigDecimal.ONE);
        item.setPrecoUnitario(
                this.precoUnitario != null ? this.precoUnitario : produto.getPrecoVenda()
        );
        item.setObservacao(this.observacao);
        item.setStatus(ItemPedido.StatusItemPedido.PENDENTE);

        // Se for um combo, o preço pode ser diferente
        if (this.comboId != null) {
            // TODO: Buscar combo e aplicar preço especial
        }

        item.calcularSubtotal();
        return item;
    }
}