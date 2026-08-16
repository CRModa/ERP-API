package reset.reset.dto.filter;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MovimentoStockFilter extends BaseFilter {
    private Long empresaId;
    private Long produtoId;
    private Long armazemId;
    private String tipo;
    private String referencia;
    private BigDecimal quantidadeMinima;
    private BigDecimal quantidadeMaxima;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}
