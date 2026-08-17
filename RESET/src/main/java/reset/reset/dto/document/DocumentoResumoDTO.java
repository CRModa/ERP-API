package reset.reset.dto.document;

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
public class DocumentoResumoDTO {
    private Long id;
    private String numero;
    private LocalDate data;
    private BigDecimal total;
    private String estado;
    private String clienteNome;
    private String tipoDescricao;
    private Integer quantidadeItens;
}
