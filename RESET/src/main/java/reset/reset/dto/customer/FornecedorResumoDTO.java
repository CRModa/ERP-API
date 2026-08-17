package reset.reset.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FornecedorResumoDTO {
    private Long id;
    private String nome;
    private String nuit;
    private String telefone;
    private String email;
    private Boolean ativo;
}
