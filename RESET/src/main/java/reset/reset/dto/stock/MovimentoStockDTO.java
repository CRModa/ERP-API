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
public class MovimentoStockDTO {
    private Long id;
    private String tipo;
    private BigDecimal quantidade;
    private String referencia;
    private LocalDateTime dataMovimento;
    private Long produtoId;
    private String produtoNome;
    private Long armazemId;
    private String armazemNome;
    private Long empresaId;
    private String empresaNome;

    public static MovimentoStockDTO fromEntity(MovimentoStock movimento) {
        return MovimentoStockDTO.builder()
                .id(movimento.getId())
                .tipo(movimento.getTipo())
                .quantidade(movimento.getQuantidade())
                .referencia(movimento.getReferencia())
                .dataMovimento(movimento.getDataMovimento())
                .produtoId(movimento.getProduto() != null ? movimento.getProduto().getId() : null)
                .produtoNome(movimento.getProduto() != null ? movimento.getProduto().getNome() : null)
                .armazemId(movimento.getArmazem() != null ? movimento.getArmazem().getId() : null)
                .armazemNome(movimento.getArmazem() != null ? movimento.getArmazem().getNome() : null)
                .empresaId(movimento.getEmpresa() != null ? movimento.getEmpresa().getId() : null)
                .empresaNome(movimento.getEmpresa() != null ? movimento.getEmpresa().getNome() : null)
                .build();
    }
}
