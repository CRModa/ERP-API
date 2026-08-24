package reset.reset.Models.financial;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Documento;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "movimento_conta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentoConta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Column(length = 20)
    private String tipo; // ENTRADA, SAIDA

    @Column(precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "data")
    private LocalDate data;

    private String observacao;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;
}
