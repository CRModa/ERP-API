package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoDTO {
    private Long id;
    private Long pedidoId;
    private Long produtoId;
    private String produtoNome;
    private String produtoCodigo;
    private Long comboId;
    private String comboNome;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal descontoValor;
    private BigDecimal subtotal;
    private String observacao;
    private String status;
    private Long descontoId;
    private Long ivaId;
    private String ivaCodigo;
    private BigDecimal ivaTaxa;
}
