package reset.reset.dto.auth;

import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String nome;
    private String email;
    private Long empresaId;
    private Set<String> roleNames;
    private Set<String> permissionNames;
}
