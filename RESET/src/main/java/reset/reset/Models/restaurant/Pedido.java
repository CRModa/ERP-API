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

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "atendente_id")
    private User atendente;

    @ManyToOne
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
    private BigDecimal subtotal;

    @Column(name = "desconto", precision = 15, scale = 2)
    private BigDecimal desconto;

    @Column(name = "taxa_servico", precision = 15, scale = 2)
    private BigDecimal taxaServico;

    @Column(name = "total", precision = 15, scale = 2)
    private BigDecimal total;

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

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPedido> itens = new ArrayList<>();

    public enum TipoPedido {
        MESA,
        DELIVERY,
        TAKEAWAY
    }

    public enum StatusPedido {
        PENDENTE,
        EM_PREPARO,
        PRONTO,
        ENTREGUE,
        CANCELADO,
        FECHADO
    }
}