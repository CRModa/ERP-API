package reset.reset.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.auth.Permissao;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissaoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private String recurso;
    private String acao;

    public static PermissaoDTO fromEntity(Permissao permissao) {
        return PermissaoDTO.builder()
                .id(permissao.getId())
                .nome(permissao.getNome())
                .descricao(permissao.getDescricao())
                .recurso(permissao.getRecurso())
                .acao(permissao.getAcao())
                .build();
    }
}
