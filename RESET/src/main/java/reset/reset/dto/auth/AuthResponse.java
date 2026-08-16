package reset.reset.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private String nome;
    private Long empresaId;
    private String empresaNome;
    private Set<String> roles;
    private Set<String> permissions;
    private Boolean useCookie = true;
}
