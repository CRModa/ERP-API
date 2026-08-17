package reset.reset.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.auth.Role;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String nome;
    private String descricao;
    private Boolean ativo;
    private Set<String> permissoes;

    public static RoleDTO fromEntity(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .nome(role.getNome())
                .descricao(role.getDescricao())
                .ativo(role.getAtivo())
                .permissoes(role.getPermissoes() != null ?
                        role.getPermissoes().stream().map(p -> p.getNome()).collect(Collectors.toSet()) : null)
                .build();
    }
}
