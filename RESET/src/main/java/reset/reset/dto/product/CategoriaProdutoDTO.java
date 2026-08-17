package reset.reset.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.CategoriaProduto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaProdutoDTO {
    private Long id;
    private String codigo;
    private String descricao;
    private Long empresaId;
    private String empresaNome;
    private Long totalProdutos;

    public static CategoriaProdutoDTO fromEntity(CategoriaProduto categoria) {
        return CategoriaProdutoDTO.builder()
                .id(categoria.getId())
                .codigo(categoria.getCodigo())
                .descricao(categoria.getDescricao())
                .empresaId(categoria.getEmpresa() != null ? categoria.getEmpresa().getId() : null)
                .empresaNome(categoria.getEmpresa() != null ? categoria.getEmpresa().getNome() : null)
                .build();
    }
}

