package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.restaurant.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Long id;
    private String numero;
    private String tipo;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal taxaServico;
    private BigDecimal total;
    private String observacao;
    private LocalDateTime dataPedido;
    private LocalDateTime dataEntrega;
    private LocalDateTime dataFechamento;
    private Integer tempoEspera;
    private Long empresaId;
    private String empresaNome;
    private Long mesaId;
    private String mesaNumero;
    private Long clienteId;
    private String clienteNome;
    private String clienteTelefone;
    private Long atendenteId;
    private String atendenteNome;
    private Long garcomId;
    private String garcomNome;
    private List<ItemPedidoDTO> itens;
    private Integer quantidadeItens;

    public static PedidoDTO fromEntity(Pedido pedido) {
        if (pedido == null) return null;

        return PedidoDTO.builder()
                .id(pedido.getId())
                .numero(pedido.getNumero())
                .tipo(pedido.getTipo() != null ? pedido.getTipo().name() : null)
                .status(pedido.getStatus() != null ? pedido.getStatus().name() : null)
                .subtotal(pedido.getSubtotal())
                .desconto(pedido.getDesconto())
                .taxaServico(pedido.getTaxaServico())
                .total(pedido.getTotal())
                .observacao(pedido.getObservacao())
                .dataPedido(pedido.getDataPedido())
                .dataEntrega(pedido.getDataEntrega())
                .dataFechamento(pedido.getDataFechamento())
                .tempoEspera(pedido.getTempoEspera())
                .empresaId(pedido.getEmpresa() != null ? pedido.getEmpresa().getId() : null)
                .empresaNome(pedido.getEmpresa() != null ? pedido.getEmpresa().getNome() : null)
                .mesaId(pedido.getMesa() != null ? pedido.getMesa().getId() : null)
                .mesaNumero(pedido.getMesa() != null ? pedido.getMesa().getNumero() : null)
                .clienteId(pedido.getCliente() != null ? pedido.getCliente().getId() : null)
                .clienteNome(pedido.getCliente() != null ? pedido.getCliente().getNome() : null)
                .clienteTelefone(pedido.getCliente() != null ? pedido.getCliente().getTelefone() : null)
                .atendenteId(pedido.getAtendente() != null ? pedido.getAtendente().getId() : null)
                .atendenteNome(pedido.getAtendente() != null ? pedido.getAtendente().getNome() : null)
                .garcomId(pedido.getGarcom() != null ? pedido.getGarcom().getId() : null)
                .garcomNome(pedido.getGarcom() != null ? pedido.getGarcom().getNome() : null)
                .itens(pedido.getItens() != null ?
                        pedido.getItens().stream()
                                .map(ItemPedidoDTO::fromEntity)
                                .collect(Collectors.toList()) : null)
                .quantidadeItens(pedido.getItens() != null ? pedido.getItens().size() : 0)
                .build();
    }
}

