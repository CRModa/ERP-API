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
@Table(name = "desconto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Desconto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(length = 150)
    private String descricao;

    @Column(length = 20)
    private String tipo; // PERCENTAGEM / VALOR

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @CreationTimestamp
    @Column(name = "data_registo", updatable = false)
    private LocalDateTime dataRegisto;
}
