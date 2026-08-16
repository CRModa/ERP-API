package reset.reset.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferenciaStockRequest {
    @NotNull(message = "Produto ID is required")
    private Long produtoId;

    @NotNull(message = "Origem Armazem ID is required")
    private Long origemArmazemId;

    @NotNull(message = "Destino Armazem ID is required")
    private Long destinoArmazemId;

    @NotNull(message = "Quantidade is required")
    @Positive(message = "Quantidade must be greater than zero")
    private BigDecimal quantidade;

    private String referencia;
}
