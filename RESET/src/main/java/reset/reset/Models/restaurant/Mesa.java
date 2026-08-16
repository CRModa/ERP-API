package reset.reset.Models.restaurant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;

import java.time.LocalDateTime;

@Entity
@Table(name = "rest_mesa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "numero", nullable = false, length = 10)
    private String numero;

    @Column(name = "capacidade")
    private Integer capacidade = 4;

    @Column(name = "localizacao", length = 100)
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusMesa status = StatusMesa.DISPONIVEL;

    @Column(name = "ativo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum StatusMesa {
        DISPONIVEL,
        OCUPADA,
        RESERVADA,
        EM_LIMPEZA,
        INDISPONIVEL
    }
}
