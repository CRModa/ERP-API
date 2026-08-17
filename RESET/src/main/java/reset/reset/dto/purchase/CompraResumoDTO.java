package reset.reset.dto.purchase;

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
public class CompraResumoDTO {
    private Long id;
    private LocalDate data;
    private BigDecimal total;
    private String estado;
    private String fornecedorNome;
    private Integer quantidadeItens;
}
