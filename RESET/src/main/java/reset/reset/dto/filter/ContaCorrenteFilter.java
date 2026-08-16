package reset.reset.dto.filter;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContaCorrenteFilter extends BaseFilter {
    private Long empresaId;
    private Long clienteId;
    private Long fornecedorId;
    private Long documentoId;
    private String tipoMovimento;
    private BigDecimal valorMinimo;
    private BigDecimal valorMaximo;
    private Boolean pago;
    private LocalDate dataMovimentoInicio;
    private LocalDate dataMovimentoFim;
    private LocalDate dataVencimentoInicio;
    private LocalDate dataVencimentoFim;
    private LocalDate dataPagamentoInicio;
    private LocalDate dataPagamentoFim;
}
