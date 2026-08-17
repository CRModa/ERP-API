package reset.reset.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaEstatisticasDTO {
    private Long totalEmpresas;
    private Long empresasAtivas;
    private Long empresasInativas;
}
