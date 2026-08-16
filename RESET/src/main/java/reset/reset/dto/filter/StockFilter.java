package reset.reset.dto.filter;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockFilter extends BaseFilter {
    private Long empresaId;
    private Long produtoId;
    private Long armazemId;
    private BigDecimal quantidadeMinima;
    private BigDecimal quantidadeMaxima;
    private Boolean hasStock = true;
    private Boolean lowStock = false;
    private Integer lowStockThreshold = 10;
}
