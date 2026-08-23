package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.restaurant.ItemPedido;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoDTO {
    private Long id;
    private Long pedidoId;
    private Long produtoId;
    private String produtoNome;
    private String produtoCodigo;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal descontoValor;
    private BigDecimal subtotal;
    private String observacao;
    private String status;
    private Long descontoId;
    private Long ivaId;
    private String ivaCodigo;
    private BigDecimal ivaTaxa;
    private Boolean isComposto;

    public static ItemPedidoDTO fromEntity(ItemPedido item) {
        if (item == null) return null;

        return ItemPedidoDTO.builder()
                .id(item.getId())
                .pedidoId(item.getPedido() != null ? item.getPedido().getId() : null)
                .produtoId(item.getProduto() != null ? item.getProduto().getId() : null)
                .produtoNome(item.getProduto() != null ? item.getProduto().getNome() : null)
                .produtoCodigo(item.getProduto() != null ? item.getProduto().getCodigo() : null)
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .descontoValor(item.getDescontoValor())
                .subtotal(item.getSubtotal())
                .observacao(item.getObservacao())
                .status(item.getStatus() != null ? item.getStatus().name() : null)
                .descontoId(item.getDesconto() != null ? item.getDesconto().getId() : null)
                .ivaId(item.getIva() != null ? item.getIva().getId() : null)
                .ivaCodigo(item.getIva() != null ? item.getIva().getCodigo() : null)
                .ivaTaxa(item.getIva() != null ? item.getIva().getTaxa() : null)
                .isComposto(item.getProduto() != null && Boolean.TRUE.equals(item.getProduto().getIsComposto()))
                .build();
    }
}
