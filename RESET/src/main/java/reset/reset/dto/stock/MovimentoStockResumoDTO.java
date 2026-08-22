package reset.reset.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.stock.MovimentoStock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentoStockResumoDTO {
    private Long id;
    private String tipo;
    private BigDecimal quantidade;
    private String referencia;
    private LocalDateTime dataMovimento;
    private String produtoNome;
    private String armazemNome;

    public static MovimentoStockResumoDTO fromEntity(MovimentoStock movimento) {
        return MovimentoStockResumoDTO.builder()
                .id(movimento.getId())
                .tipo(movimento.getTipo())
                .quantidade(movimento.getQuantidade())
                .referencia(movimento.getReferencia())
                .dataMovimento(movimento.getDataMovimento())
                .produtoNome(movimento.getProduto() != null ? movimento.getProduto().getNome() : null)
                .armazemNome(movimento.getArmazem() != null ? movimento.getArmazem().getNome() : null)
                .build();
    }
}