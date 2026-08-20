package reset.reset.Models.restaurant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.Iva;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rest_item_cardapio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCardapio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaCardapio categoria;

    @ManyToOne
    @JoinColumn(name = "iva_id")
    private Iva iva;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "codigo", length = 50)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco", nullable = false, precision = 15, scale = 2)
    private BigDecimal preco;

    @Column(name = "custo", precision = 15, scale = 2)
    private BigDecimal custo;

    @Column(name = "tempo_preparo")
    private Integer tempoPreparo;

    @Column(name = "ingredientes", columnDefinition = "TEXT")
    private String ingredientes;

    @Column(name = "informacao_nutricional", columnDefinition = "TEXT")
    private String informacaoNutricional;

    @Column(name = "imagem", columnDefinition = "TEXT")
    private String imagem;

    @Column(name = "destaque", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean destaque = false;

    @Column(name = "disponivel", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean disponivel = true;

    @Column(name = "ativo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPedido> itensPedido = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemCombo> itensCombo = new ArrayList<>();
}