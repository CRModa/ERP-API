package reset.reset.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.auth.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String nome;
    private String email;
    private String username;
    private String perfil;
    private Boolean ativo;
    private LocalDateTime dataRegisto;
    private LocalDateTime ultimoLogin;
    private Long empresaId;
    private String empresaNome;
    private Set<String> roles;
    private Set<String> permissoes;

    public static UserDTO fromEntity(User utilizador) {
        return UserDTO.builder()
                .id(utilizador.getId())
                .nome(utilizador.getNome())
                .email(utilizador.getEmail())
                .username(utilizador.getUsername())
                .perfil(utilizador.getPerfil())
                .ativo(utilizador.getAtivo())
                .dataRegisto(utilizador.getDataRegisto())
                .ultimoLogin(utilizador.getUltimoLogin())
                .empresaId(utilizador.getEmpresa() != null ? utilizador.getEmpresa().getId() : null)
                .empresaNome(utilizador.getEmpresa() != null ? utilizador.getEmpresa().getNome() : null)
                .roles(utilizador.getRoles() != null ?
                        utilizador.getRoles().stream().map(r -> r.getNome()).collect(Collectors.toSet()) : null)
                .permissoes(utilizador.getPermissoes() != null ?
                        utilizador.getPermissoes().stream().map(p -> p.getNome()).collect(Collectors.toSet()) : null)
                .build();
    }
}

