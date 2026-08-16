package reset.reset.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PedidoEstatisticas {
    private Long totalPedidos;
    private BigDecimal totalVendas;
    private Long pendentes;
    private Long emPreparo;
    private Long prontos;
    private Long entregues;
    private Long cancelados;
    private Long fechados;
}
