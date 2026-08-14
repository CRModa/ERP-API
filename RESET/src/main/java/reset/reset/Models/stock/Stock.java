package reset.reset.Models.stock;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;

@Entity
@Table(name = "stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "armazem_id")
    private Armazem armazem;

    @Column(name = "quantidade_atual", precision = 15, scale = 3)
    private BigDecimal quantidadeAtual = BigDecimal.ZERO;
}
