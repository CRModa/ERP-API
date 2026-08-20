package reset.reset.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.CategoriaProduto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaProdutoRequest {

    private String codigo;

    private String descricao;

    private String observacao;

    private Boolean ativo = true;

    private Boolean visivelRestaurante = false;
    private Boolean visivelPos = false;
    private Boolean visivelFarmacia = false;
    private Boolean visivelWeb = false;
    private Boolean visivelPdv = false;

    private Integer ordem = 0;


    public CategoriaProduto toEntity() {
        CategoriaProduto categoria = new CategoriaProduto();
        categoria.setCodigo(this.codigo);
        categoria.setDescricao(this.descricao);
        categoria.setObservacao(this.observacao);
        categoria.setAtivo(this.ativo != null ? this.ativo : true);
        categoria.setVisivelRestaurante(this.visivelRestaurante != null ? this.visivelRestaurante : false);
        categoria.setVisivelPos(this.visivelPos != null ? this.visivelPos : false);
        categoria.setVisivelFarmacia(this.visivelFarmacia != null ? this.visivelFarmacia : false);
        categoria.setVisivelWeb(this.visivelWeb != null ? this.visivelWeb : false);

        return categoria;
    }

    public static CategoriaProdutoRequest fromEntity(CategoriaProduto categoria) {
        return CategoriaProdutoRequest.builder()
                .codigo(categoria.getCodigo())
                .descricao(categoria.getDescricao())
                .observacao(categoria.getObservacao())
                .ativo(categoria.getAtivo())
                .visivelRestaurante(categoria.getVisivelRestaurante())
                .visivelPos(categoria.getVisivelPos())
                .visivelFarmacia(categoria.getVisivelFarmacia())
                .visivelWeb(categoria.getVisivelWeb())
                .build();
    }
}