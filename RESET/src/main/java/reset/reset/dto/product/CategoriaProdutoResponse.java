package reset.reset.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.CategoriaProduto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaProdutoResponse {
    private Long id;
    private String codigo;
    private String descricao;
    private String observacao;
    private Boolean ativo;
    private Boolean visivelRestaurante;
    private Boolean visivelPos;
    private Boolean visivelFarmacia;
    private Boolean visivelWeb;
    private Boolean visivelPdv;
    private Integer ordem;
    private Long empresaId;
    private String empresaNome;
    private Long totalProdutos;
    private List<String> canaisVisiveis;
    private Boolean isVisivelEmAlgumCanal;

    public static CategoriaProdutoResponse fromEntity(CategoriaProduto categoria) {
        return CategoriaProdutoResponse.builder()
                .id(categoria.getId())
                .codigo(categoria.getCodigo())
                .descricao(categoria.getDescricao())
                .observacao(categoria.getObservacao())
                .ativo(categoria.getAtivo())
                .visivelRestaurante(categoria.getVisivelRestaurante())
                .visivelPos(categoria.getVisivelPos())
                .visivelFarmacia(categoria.getVisivelFarmacia())
                .visivelWeb(categoria.getVisivelWeb())
                .empresaId(categoria.getEmpresa() != null ? categoria.getEmpresa().getId() : null)
                .empresaNome(categoria.getEmpresa() != null ? categoria.getEmpresa().getNome() : null)
                .totalProdutos(categoria.getProdutos() != null ? (long) categoria.getProdutos().size() : 0L)
                .build();
    }
}

