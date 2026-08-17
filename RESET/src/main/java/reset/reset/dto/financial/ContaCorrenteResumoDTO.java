package reset.reset.dto.financial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaCorrenteResumoDTO {
    private Long id;
    private String tipoMovimento;
    private BigDecimal valor;
    private String descricao;
    private Boolean pago;
    private LocalDate dataMovimento;
    private LocalDate dataVencimento;
    private String clienteNome;
    private String fornecedorNome;
}
