package reset.reset.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Nome is required")
    @Size(max = 150, message = "Nome must be less than 150 characters")
    private String nome;

    @Email(message = "Invalid email format")
    private String email;

    private Long empresaId;
    private Set<String> roleNames;
    private Set<String> permissionNames;
}
