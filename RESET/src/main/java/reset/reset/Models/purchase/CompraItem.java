package reset.reset.Models.purchase;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.Iva;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;

@Entity
@Table(name = "compra_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", precision = 15, scale = 2)
    private BigDecimal precoUnitario;

    @ManyToOne
    @JoinColumn(name = "iva_id")
    private Iva iva;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;
}
