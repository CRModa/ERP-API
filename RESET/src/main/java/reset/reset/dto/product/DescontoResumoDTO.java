package reset.reset.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescontoResumoDTO {
    private Long id;
    private String descricao;
    private String tipo;
    private BigDecimal valor;
    private Boolean ativo;
}
