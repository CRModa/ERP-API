package reset.reset.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AjusteStockRequest {
    @NotNull(message = "Produto ID is required")
    private Long produtoId;

    @NotNull(message = "Armazem ID is required")
    private Long armazemId;

    @NotNull(message = "Nova quantidade is required")
    @PositiveOrZero(message = "Quantidade must be zero or positive")
    private BigDecimal novaQuantidade;

    private String motivo;
}
