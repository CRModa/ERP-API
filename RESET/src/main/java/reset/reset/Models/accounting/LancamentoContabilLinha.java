package reset.reset.Models.accounting;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "lancamento_contabil_linha")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoContabilLinha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lancamento_id", nullable = false)
    private LancamentoContabil lancamento;

    @ManyToOne
    @JoinColumn(name = "conta_contabil_id", nullable = false)
    private ContaContabil contaContabil;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(length = 1, nullable = false)
    @Enumerated(EnumType.STRING)
    private Natureza natureza;

    @Column(length = 200)
    private String descricao;

    public enum Natureza {
        D, C
    }
}
