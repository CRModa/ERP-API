package reset.reset.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.CategoriaProduto;
import reset.reset.Models.product.Iva;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;

@Data
public class ProdutoRequest {
    @NotBlank(message = "Nome is required")
    @Size(max = 200, message = "Nome must be less than 200 characters")
    private String nome;

    private String codigo;
    private String descricao;
    private BigDecimal precoVenda;
    private BigDecimal precoCusto;

    @NotNull(message = "Categoria ID is required")
    private Long categoriaId;

    @NotNull(message = "IVA ID is required")
    private Long ivaId;

    public Produto toEntity() {
        Produto produto = new Produto();
        produto.setNome(this.nome);
        produto.setCodigo(this.codigo);
        produto.setDescricao(this.descricao);
        produto.setPrecoVenda(this.precoVenda != null ? this.precoVenda : BigDecimal.ZERO);
        produto.setPrecoCusto(this.precoCusto != null ? this.precoCusto : BigDecimal.ZERO);

        Empresa empresa = new Empresa();
        produto.setEmpresa(empresa);

        CategoriaProduto categoria = new CategoriaProduto();
        categoria.setId(this.categoriaId);
        produto.setCategoria(categoria);

        Iva iva = new Iva();
        iva.setId(this.ivaId);
        produto.setIva(iva);

        return produto;
    }
}
