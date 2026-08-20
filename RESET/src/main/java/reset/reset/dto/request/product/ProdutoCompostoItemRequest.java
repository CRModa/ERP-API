package reset.reset.dto.request.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProdutoCompostoItemRequest {

//    @NotNull(message = "Produto filho ID é obrigatório")
    private Long produtoFilhoId;

//    @NotNull(message = "Quantidade é obrigatória")
//    @Positive(message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade = BigDecimal.ONE;

    private BigDecimal precoAdicional = BigDecimal.ZERO;

    private Boolean obrigatorio = true;
}
