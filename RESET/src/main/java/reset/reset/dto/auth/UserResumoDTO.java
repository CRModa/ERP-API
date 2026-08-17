package reset.reset.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResumoDTO {
    private Long id;
    private String nome;
    private String username;
    private String email;
    private String perfil;
    private Boolean ativo;
    private String empresaNome;
}
