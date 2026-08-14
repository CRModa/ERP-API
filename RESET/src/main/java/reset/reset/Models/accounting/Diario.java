package reset.reset.Models.accounting;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.core.Empresa;

@Entity
@Table(name = "diario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(length = 20)
    private String codigo;

    @Column(length = 100)
    private String descricao;
}