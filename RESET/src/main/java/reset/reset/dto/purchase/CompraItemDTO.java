package reset.reset.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.purchase.CompraItem;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraItemDTO {
    private Long id;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private Long produtoId;
    private String produtoNome;
    private String produtoCodigo;
    private Long ivaId;
    private String ivaCodigo;
    private BigDecimal ivaTaxa;

    public static CompraItemDTO fromEntity(CompraItem item) {
        return CompraItemDTO.builder()
                .id(item.getId())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .produtoId(item.getProduto() != null ? item.getProduto().getId() : null)
                .produtoNome(item.getProduto() != null ? item.getProduto().getNome() : null)
                .produtoCodigo(item.getProduto() != null ? item.getProduto().getCodigo() : null)
                .ivaId(item.getIva() != null ? item.getIva().getId() : null)
                .ivaCodigo(item.getIva() != null ? item.getIva().getCodigo() : null)
                .ivaTaxa(item.getIva() != null ? item.getIva().getTaxa() : null)
                .build();
    }
}
