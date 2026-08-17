package reset.reset.dto.accounting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaContabilResumoDTO {
    private Long id;
    private String codigo;
    private String descricao;
    private String tipo;
}
