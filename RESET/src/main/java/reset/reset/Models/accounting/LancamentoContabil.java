package reset.reset.Models.accounting;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.auth.User;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.document.Documento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lancamento_contabil")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoContabil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "diario_id")
    private Diario diario;

    @Column(name = "numero_lancamento", length = 50, nullable = false)
    private String numeroLancamento;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(name = "data_valor")
    private LocalDate dataValor;

    @Column(length = 200, nullable = false)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @ManyToOne
    @JoinColumn(name = "utilizador_id")
    private User utilizador;

    @CreationTimestamp
    @Column(name = "data_registo", updatable = false)
    private LocalDateTime dataRegisto;

    @OneToMany(mappedBy = "lancamento", cascade = CascadeType.ALL)
    private List<LancamentoContabilLinha> linhas;
}

