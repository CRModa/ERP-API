package reset.reset.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentoStockResumoDTO {
    private Long id;
    private String tipo;
    private BigDecimal quantidade;
    private String referencia;
    private LocalDateTime dataMovimento;
    private String produtoNome;
    private String armazemNome;
}
