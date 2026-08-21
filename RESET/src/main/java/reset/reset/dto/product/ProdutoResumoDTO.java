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
public class ProdutoResumoDTO {
    private Long id;
    private String codigo;
    private String nome;
    private BigDecimal precoVenda;
    private String categoriaNome;
    private Boolean ativo;
    private Boolean disponivel;
    private BigDecimal quantidadeEstoque;
    private BigDecimal precoCusto;
}
