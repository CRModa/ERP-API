package reset.reset.dto.filter;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProdutoFilter extends BaseFilter {
    private String codigo;
    private String nome;
    private String descricao;
    private Long empresaId;
    private Long categoriaId;
    private Long ivaId;
    private BigDecimal precoVendaMinimo;
    private BigDecimal precoVendaMaximo;
    private BigDecimal precoCustoMinimo;
    private BigDecimal precoCustoMaximo;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}
