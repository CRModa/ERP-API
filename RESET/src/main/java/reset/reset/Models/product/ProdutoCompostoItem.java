package reset.reset.Models.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "produto_composto_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoCompostoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_pai_id", nullable = false)
    private Produto produtoPai;

    @ManyToOne
    @JoinColumn(name = "produto_filho_id", nullable = false)
    private Produto produtoFilho;

    @Column(name = "quantidade", nullable = false)
    private BigDecimal quantidade = BigDecimal.ONE;

    @Column(name = "preco_adicional", precision = 15, scale = 2)
    private BigDecimal precoAdicional = BigDecimal.ZERO;

    @Column(name = "obrigatorio", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean obrigatorio = true;
}