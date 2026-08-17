package reset.reset.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResumoDTO {
    private Long produtoId;
    private String produtoNome;
    private String produtoCodigo;
    private Long armazemId;
    private String armazemNome;
    private BigDecimal quantidadeAtual;
    private BigDecimal precoVenda;
    private BigDecimal valorTotal;
}
