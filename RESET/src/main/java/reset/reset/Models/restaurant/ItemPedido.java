package reset.reset.Models.restaurant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.Desconto;
import reset.reset.Models.product.Iva;

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

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private ItemCardapio item;

    @ManyToOne
    @JoinColumn(name = "combo_id")
    private Combo combo;

    @ManyToOne
    @JoinColumn(name = "desconto_id")
    private Desconto desconto;

    @ManyToOne
    @JoinColumn(name = "iva_id")
    private Iva iva;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade = 1;

    @Column(name = "preco_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "desconto_valor", precision = 15, scale = 2)
    private BigDecimal descontoValor;

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
        PENDENTE,
        EM_PREPARO,
        PRONTO,
        ENTREGUE,
        CANCELADO
    }
}