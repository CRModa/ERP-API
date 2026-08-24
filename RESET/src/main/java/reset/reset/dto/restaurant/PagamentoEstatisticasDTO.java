package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoEstatisticasDTO {
    private Long totalPagamentos;
    private BigDecimal valorTotal;
    private BigDecimal valorTotalHoje;
    private Map<String, Long> pagamentosPorMetodo;
    private Map<String, BigDecimal> valorPorMetodo;
    private Long totalPedidosPagos;
}
