package reset.reset.dto.filter;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DocumentoFilter extends BaseFilter {
    private String numero;
    private Long empresaId;
    private Long clienteId;
    private Long tipoId;
    private String estado;
    private BigDecimal totalMinimo;
    private BigDecimal totalMaximo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDateTime dataRegistoInicio;
    private LocalDateTime dataRegistoFim;
}
