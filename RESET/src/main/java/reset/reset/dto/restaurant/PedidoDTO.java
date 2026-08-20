package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private Long atendenteId;
    private String atendenteNome;
    private Long garcomId;
    private String garcomNome;
    private List<ItemPedidoDTO> itens;
}
