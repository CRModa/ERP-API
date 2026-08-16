package reset.reset.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.auth.User;

import java.util.Set;

@Data
public class UpdateUserRequest {
    @Size(max = 150, message = "Nome must be less than 150 characters")
    private String nome;

    @Email(message = "Invalid email format")
    private String email;

    private Set<String> roleNames;
    private Set<String> permissionNames;
    private Long empresaId;

    public User toEntity() {
        User utilizador = new User();
        utilizador.setNome(this.nome);
        utilizador.setEmail(this.email);
        return utilizador;
    }
}
