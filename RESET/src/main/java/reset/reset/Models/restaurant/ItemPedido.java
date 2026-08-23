package reset.reset.Models.restaurant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.Desconto;
import reset.reset.Models.product.Iva;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;

@Entity
@Table(name = "rest_item_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id")
    private Combo combo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desconto_id")
    private Desconto desconto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iva_id")
    private Iva iva;

    @Column(name = "quantidade", nullable = false)
    private BigDecimal quantidade = BigDecimal.ONE;

    @Column(name = "preco_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "desconto_valor", precision = 15, scale = 2)
    private BigDecimal descontoValor = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusItemPedido status = StatusItemPedido.PENDENTE;

    public enum StatusItemPedido {
        PENDENTE, EM_PREPARO, PRONTO, ENTREGUE, CANCELADO
    }

    public void calcularSubtotal() {
        BigDecimal precoBase = this.precoUnitario != null ? this.precoUnitario : BigDecimal.ZERO;
        BigDecimal qtd = this.quantidade != null ? this.quantidade : BigDecimal.ONE;

        BigDecimal subtotalItem = precoBase.multiply(qtd);

        if (this.descontoValor != null && this.descontoValor.compareTo(BigDecimal.ZERO) > 0) {
            subtotalItem = subtotalItem.subtract(this.descontoValor);
        }

        this.subtotal = subtotalItem;
    }
}