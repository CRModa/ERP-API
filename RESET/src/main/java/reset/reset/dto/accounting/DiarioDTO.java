package reset.reset.dto.accounting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.accounting.Diario;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiarioDTO {
    private Long id;
    private String codigo;
    private String descricao;
    private Long empresaId;
    private String empresaNome;

    public static DiarioDTO fromEntity(Diario diario) {
        return DiarioDTO.builder()
                .id(diario.getId())
                .codigo(diario.getCodigo())
                .descricao(diario.getDescricao())
                .empresaId(diario.getEmpresa() != null ? diario.getEmpresa().getId() : null)
                .empresaNome(diario.getEmpresa() != null ? diario.getEmpresa().getNome() : null)
                .build();
    }
}
