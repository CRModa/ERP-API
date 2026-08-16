package reset.reset.dto.request.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.restaurant.CategoriaCardapio;

@Data
public class CategoriaCardapioRequest {

    @NotBlank(message = "Nome da categoria é obrigatório")
    private String nome;

    private String descricao;
    private Integer ordem;
    private String icone;

    @NotNull(message = "Empresa ID é obrigatório")
    private Long empresaId;

    public CategoriaCardapio toEntity() {
        CategoriaCardapio categoria = new CategoriaCardapio();
        categoria.setNome(this.nome);
        categoria.setDescricao(this.descricao);
        categoria.setOrdem(this.ordem != null ? this.ordem : 0);
        categoria.setIcone(this.icone);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        categoria.setEmpresa(empresa);

        return categoria;
    }
}
