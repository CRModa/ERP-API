package reset.reset.Models.customer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(length = 200, nullable = false)
    private String nome;

    @Column(length = 20)
    private String nuit;

    @Column(columnDefinition = "TEXT")
    private String endereco;

    @Column(length = 50)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String tipo; // PARTICULAR / EMPRESA

    @Column(name = "desconto_padrao", precision = 5, scale = 2)
    private BigDecimal descontoPadrao = BigDecimal.ZERO;

    @Column(name = "limite_credito", precision = 15, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "saldo_corrente", precision = 15, scale = 2)
    private BigDecimal saldoCorrente = BigDecimal.ZERO;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "data_registo", updatable = false)
    private LocalDateTime dataRegisto;
}
