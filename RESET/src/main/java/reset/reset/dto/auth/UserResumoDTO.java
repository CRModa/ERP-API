package reset.reset.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.auth.User;

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
    private String empresaId;  // Adicionado para facilitar navegação
    private String ultimoLogin; // Adicionado para informação de atividade
    private Integer quantidadeRoles; // Adicionado para indicar quantidade de permissões

    public static UserResumoDTO fromEntity(User user) {
        if (user == null) return null;

        return UserResumoDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .username(user.getUsername())
                .email(user.getEmail())
                .perfil(user.getPerfil())
                .ativo(user.getAtivo())
                .empresaNome(user.getEmpresa() != null ? user.getEmpresa().getNome() : null)
                .empresaId(user.getEmpresa() != null ? user.getEmpresa().getId().toString() : null)
                .ultimoLogin(user.getUltimoLogin() != null ? user.getUltimoLogin().toString() : null)
                .quantidadeRoles(user.getRoles() != null ? user.getRoles().size() : 0)
                .build();
    }

    public static UserResumoDTO fromEntityCompact(User user) {
        if (user == null) return null;

        return UserResumoDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .username(user.getUsername())
                .email(user.getEmail())
                .ativo(user.getAtivo())
                .empresaNome(user.getEmpresa() != null ? user.getEmpresa().getNome() : null)
                .build();
    }
}