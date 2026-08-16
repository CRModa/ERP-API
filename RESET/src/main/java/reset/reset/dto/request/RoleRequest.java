package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.auth.Role;

import java.util.Set;

@Data
public class RoleRequest {
    @NotBlank(message = "Nome is required")
    @Size(max = 50, message = "Nome must be less than 50 characters")
    private String nome;

    @Size(max = 200, message = "Descricao must be less than 200 characters")
    private String descricao;

    private Set<String> permissionNames;

    public Role toEntity() {
        Role role = new Role();
        role.setNome(this.nome);
        role.setDescricao(this.descricao);
        return role;
    }
}
