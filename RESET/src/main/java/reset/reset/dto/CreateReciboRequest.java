package reset.reset.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateReciboRequest {
    private Long empresaId;
    private Long clienteId;
    private BigDecimal valorPago;
    private String formaPagamento;
    private String referenciaPagamento;
    private LocalDate data;
}
