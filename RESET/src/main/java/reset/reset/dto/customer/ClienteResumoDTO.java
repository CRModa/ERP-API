package reset.reset.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResumoDTO {
    private Long id;
    private String nome;
    private String nuit;
    private String telefone;
    private String email;
    private String tipo;
    private BigDecimal saldoCorrente;
    private Boolean ativo;
}
