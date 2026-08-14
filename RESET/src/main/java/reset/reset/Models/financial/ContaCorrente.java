package reset.reset.Models.financial;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;
import reset.reset.Models.customer.Fornecedor;
import reset.reset.Models.document.Documento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conta_corrente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContaCorrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Column(name = "data_movimento", nullable = false)
    private LocalDate dataMovimento;

    @Column(name = "tipo_movimento", length = 20)
    @Enumerated(EnumType.STRING)
    private TipoMovimento tipoMovimento;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_anterior", precision = 15, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_atual", precision = 15, scale = 2)
    private BigDecimal saldoAtual;

    @Column(length = 200)
    private String descricao;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean pago = false;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum TipoMovimento {
        DEBITO, CREDITO
    }
}
