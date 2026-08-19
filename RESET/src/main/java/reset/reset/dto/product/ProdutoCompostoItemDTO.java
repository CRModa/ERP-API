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
public class ProdutoCompostoItemDTO {
    private Long id;
    private Long produtoFilhoId;
    private String produtoFilhoNome;
    private String produtoFilhoCodigo;
    private BigDecimal quantidade;
    private BigDecimal precoAdicional;
    private Boolean obrigatorio;
}
