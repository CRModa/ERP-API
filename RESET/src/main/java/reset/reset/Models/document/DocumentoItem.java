package reset.reset.Models.document;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.Desconto;
import reset.reset.Models.product.Iva;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;

@Entity
@Table(name = "documento_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", precision = 15, scale = 2)
    private BigDecimal precoUnitario;

    @ManyToOne
    @JoinColumn(name = "desconto_id")
    private Desconto desconto;

    @ManyToOne
    @JoinColumn(name = "iva_id")
    private Iva iva;
}
