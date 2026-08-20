package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.dto.product.ProdutoRestauranteDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRestauranteDTO {
    private Long id;
    private String codigo;
    private String descricao;
    private Boolean visivelRestaurante;
    private Long totalProdutos;
    private List<ProdutoRestauranteDTO> produtos;
}
