package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.auth.Permissao;

@Data
public class PermissaoRequest {
    @NotBlank(message = "Nome is required")
    @Size(max = 100, message = "Nome must be less than 100 characters")
    private String nome;

    @Size(max = 200, message = "Descricao must be less than 200 characters")
    private String descricao;

    @Size(max = 50, message = "Recurso must be less than 50 characters")
    private String recurso;

    @Size(max = 20, message = "Acao must be less than 20 characters")
    private String acao;

    public Permissao toEntity() {
        Permissao permissao = new Permissao();
        permissao.setNome(this.nome);
        permissao.setDescricao(this.descricao);
        permissao.setRecurso(this.recurso);
        permissao.setAcao(this.acao);
        return permissao;
    }
}
