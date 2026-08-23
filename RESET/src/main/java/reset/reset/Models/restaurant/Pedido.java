package reset.reset.Models.restaurant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import reset.reset.Models.auth.User;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rest_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendente_id")
    private User atendente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garcom_id")
    private User garcom;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoPedido tipo = TipoPedido.MESA;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "desconto", precision = 15, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(name = "taxa_servico", precision = 15, scale = 2)
    private BigDecimal taxaServico = BigDecimal.ZERO;

    @Column(name = "total", precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "data_pedido")
    private LocalDateTime dataPedido;

    @Column(name = "data_entrega")
    private LocalDateTime dataEntrega;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "tempo_espera")
    private Integer tempoEspera;

    @Column(name = "ativo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public enum TipoPedido {
        MESA, DELIVERY, TAKEAWAY
    }

    public enum StatusPedido {
        PENDENTE, EM_PREPARO, PRONTO, ENTREGUE, CANCELADO, FECHADO
    }

    // Métodos utilitários
    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        this.itens.add(item);
        this.recalcularTotais();
    }

    public void removerItem(Long itemId) {
        this.itens.removeIf(item -> item.getId().equals(itemId));
        this.recalcularTotais();
    }

    public void recalcularTotais() {
        BigDecimal subtotalCalculado = this.itens.stream()
                .map(ItemPedido::getSubtotal)
                .filter(s -> s != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.subtotal = subtotalCalculado;

        BigDecimal totalComDesconto = this.subtotal.subtract(
                this.desconto != null ? this.desconto : BigDecimal.ZERO
        );

        // Taxa de serviço (10% para pedidos de mesa)
        if (this.tipo == TipoPedido.MESA) {
            this.taxaServico = totalComDesconto.multiply(new BigDecimal("0.10"));
            this.total = totalComDesconto.add(this.taxaServico);
        } else {
            this.taxaServico = BigDecimal.ZERO;
            this.total = totalComDesconto;
        }
    }

    public boolean isAceitaItens() {
        return this.status == StatusPedido.PENDENTE || this.status == StatusPedido.EM_PREPARO;
    }
}