package reset.reset.dto.request.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.restaurant.Pedido;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequest {

    private Long mesaId;

    private Long clienteId;

    private Long atendenteId;

    private Long garcomId;

    @NotNull(message = "Tipo de pedido é obrigatório")
    private String tipo;

    private String observacao;

    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Valid
    private List<ItemPedidoRequest> itens;

    public Pedido toEntity() {
        Pedido pedido = new Pedido();
        pedido.setTipo(Pedido.TipoPedido.valueOf(this.tipo));
        pedido.setObservacao(this.observacao);
        pedido.setStatus(Pedido.StatusPedido.PENDENTE);
        return pedido;
    }
}
