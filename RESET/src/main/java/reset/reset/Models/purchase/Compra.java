package reset.reset.Models.purchase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Fornecedor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(name = "data")
    private LocalDate data;

    @Column(precision = 15, scale = 2)
    private BigDecimal total;

    @Column(length = 30)
    private String estado;

    @CreationTimestamp
    @Column(name = "data_registo", updatable = false)
    private LocalDateTime dataRegisto;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<CompraItem> itens;
}

