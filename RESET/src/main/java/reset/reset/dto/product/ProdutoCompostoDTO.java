package reset.reset.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoCompostoDTO {
    private Long id;
    private String codigo;
    private String nome;
    private String descricao;
    private BigDecimal precoVenda;
    private BigDecimal precoCusto;
    private Boolean ativo;
    private Boolean disponivel;
    private Boolean isComposto;
    private Integer tempoPreparo;
    private String ingredientes;
    private String imagem;
    private Boolean destaque;
    private Long categoriaId;
    private String categoriaNome;
    private Long ivaId;
    private BigDecimal ivaTaxa;
    private List<ProdutoCompostoItemDTO> itensComposto;
}
