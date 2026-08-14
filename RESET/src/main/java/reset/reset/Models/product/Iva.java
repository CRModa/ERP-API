package reset.reset.Models.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "iva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Iva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(length = 10)
    private String codigo;

    @Column(length = 100)
    private String descricao;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxa;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "data_registo", updatable = false)
    private LocalDateTime dataRegisto;
}