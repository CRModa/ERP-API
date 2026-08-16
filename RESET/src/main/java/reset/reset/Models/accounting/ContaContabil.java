package reset.reset.Models.accounting;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.core.Empresa;

@Entity
@Table(name = "conta_contabil")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContaContabil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(length = 20, nullable = false)
    private String codigo;

    @Column(length = 150, nullable = false)
    private String descricao;

    @Column(length = 30)
    private String tipo; // ATIVO, PASSIVO, CUSTO, RENDIMENTO

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;
}
