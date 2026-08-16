package reset.reset.dto.filter;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FornecedorFilter extends BaseFilter {
    private String nome;
    private String nuit;
    private String telefone;
    private String email;
    private String tipo;
    private Long empresaId;
    private BigDecimal saldoMinimo;
    private BigDecimal saldoMaximo;
    private BigDecimal limiteCreditoMinimo;
    private BigDecimal limiteCreditoMaximo;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}
