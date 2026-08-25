package reset.reset.dto.financial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.financial.MovimentoConta;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentoContaDTO {
    private Long id;
    private String tipo;
    private BigDecimal valor;
    private LocalDate data;
    private Long contaId;
    private String contaDescricao;
    private Long documentoId;
    private String documentoNumero;

    public static MovimentoContaDTO fromEntity(MovimentoConta movimento) {
        return MovimentoContaDTO.builder()
                .id(movimento.getId())
                .tipo(movimento.getTipo())
                .valor(movimento.getValor())
                .data(movimento.getData())
                .contaId(movimento.getConta() != null ? movimento.getConta().getId() : null)
                .contaDescricao(movimento.getConta() != null ? movimento.getConta().getDescricao() : null)
                .documentoId(movimento.getDocumento().getId())
                .build();
    }
}