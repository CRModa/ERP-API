package reset.reset.Models.document;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "tipo_id", nullable = false)
    private DocumentoTipo tipo;

    @Column(length = 50)
    private String numero;

    @Column(name = "data")
    private LocalDate data;

    @Column(precision = 15, scale = 2)
    private BigDecimal total;

    @Column(length = 30)
    private String estado;

    @CreationTimestamp
    @Column(name = "data_registo", updatable = false)
    private LocalDateTime dataRegisto;

    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL)
    private List<DocumentoItem> itens;
}
