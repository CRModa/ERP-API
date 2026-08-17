package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.DocumentoItem;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoItemDTO {
    private Long id;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
    private Long produtoId;
    private String produtoNome;
    private String produtoCodigo;
    private Long descontoId;
    private String descontoDescricao;
    private Long ivaId;
    private String ivaCodigo;
    private BigDecimal ivaTaxa;

    public static DocumentoItemDTO fromEntity(DocumentoItem item) {
        return DocumentoItemDTO.builder()
                .id(item.getId())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .subtotal(item.getPrecoUnitario() != null && item.getQuantidade() != null ?
                        item.getPrecoUnitario().multiply(item.getQuantidade()) : null)
                .produtoId(item.getProduto() != null ? item.getProduto().getId() : null)
                .produtoNome(item.getProduto() != null ? item.getProduto().getNome() : null)
                .produtoCodigo(item.getProduto() != null ? item.getProduto().getCodigo() : null)
                .descontoId(item.getDesconto() != null ? item.getDesconto().getId() : null)
                .descontoDescricao(item.getDesconto() != null ? item.getDesconto().getDescricao() : null)
                .ivaId(item.getIva() != null ? item.getIva().getId() : null)
                .ivaCodigo(item.getIva() != null ? item.getIva().getCodigo() : null)
                .ivaTaxa(item.getIva() != null ? item.getIva().getTaxa() : null)
                .build();
    }
}
