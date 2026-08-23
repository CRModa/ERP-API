package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.restaurant.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResumoDTO {
    private Long id;
    private String numero;
    private String status;
    private String tipo;
    private BigDecimal total;
    private LocalDateTime dataPedido;
    private String mesaNumero;
    private String clienteNome;
    private String atendenteNome;
    private Integer quantidadeItens;
    private Integer tempoEspera;

    public static PedidoResumoDTO fromEntity(Pedido pedido) {
        if (pedido == null) return null;

        return PedidoResumoDTO.builder()
                .id(pedido.getId())
                .numero(pedido.getNumero())
                .status(pedido.getStatus() != null ? pedido.getStatus().name() : null)
                .tipo(pedido.getTipo() != null ? pedido.getTipo().name() : null)
                .total(pedido.getTotal())
                .dataPedido(pedido.getDataPedido())
                .mesaNumero(pedido.getMesa() != null ? pedido.getMesa().getNumero() : null)
                .clienteNome(pedido.getCliente() != null ? pedido.getCliente().getNome() : null)
                .atendenteNome(pedido.getAtendente() != null ? pedido.getAtendente().getNome() : null)
                .quantidadeItens(pedido.getItens() != null ? pedido.getItens().size() : 0)
                .tempoEspera(pedido.getTempoEspera())
                .build();
    }
}
