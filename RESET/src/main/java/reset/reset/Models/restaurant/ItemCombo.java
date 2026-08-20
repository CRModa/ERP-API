package reset.reset.Models.restaurant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;

@Entity
@Table(name = "rest_item_combo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCombo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private ItemCardapio item;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade = 1;

    @Column(name = "preco_adicional", precision = 15, scale = 2)
    private BigDecimal precoAdicional;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;
}