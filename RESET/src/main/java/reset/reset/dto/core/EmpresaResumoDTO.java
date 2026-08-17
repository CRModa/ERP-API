package reset.reset.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResumoDTO {
    private Long id;
    private String nome;
    private String nuit;
    private String telefone;
    private String email;
    private String pais;
    private Boolean ativo;
}
