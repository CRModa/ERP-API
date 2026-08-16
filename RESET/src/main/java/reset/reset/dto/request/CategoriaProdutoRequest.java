package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.CategoriaProduto;

@Data
public class CategoriaProdutoRequest {
    @NotBlank(message = "Codigo is required")
    @Size(max = 20, message = "Codigo must be less than 20 characters")
    private String codigo;

    @NotBlank(message = "Descricao is required")
    @Size(max = 100, message = "Descricao must be less than 100 characters")
    private String descricao;

    @NotNull(message = "Empresa ID is required")
    private Long empresaId;

    public CategoriaProduto toEntity() {
        CategoriaProduto categoria = new CategoriaProduto();
        categoria.setCodigo(this.codigo);
        categoria.setDescricao(this.descricao);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        categoria.setEmpresa(empresa);

        return categoria;
    }
}

