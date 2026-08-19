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
public class ProdutoRestauranteDTO {
    private Long id;
    private String codigo;
    private String nome;
    private String descricao;
    private BigDecimal precoVenda;
    private Integer tempoPreparo;
    private String ingredientes;
    private String imagem;
    private Boolean disponivel;
    private Boolean isComposto;
    private Long categoriaId;
    private String categoriaNome;
    private List<ProdutoCompostoItemDTO> itensComposto;
}

