package reset.reset.Models.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaProduto categoria;

    @ManyToOne
    @JoinColumn(name = "iva_id")
    private Iva iva;

    @Column(length = 50)
    private String codigo;

    @Column(length = 200)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco_venda", precision = 15, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "preco_custo", precision = 15, scale = 2)
    private BigDecimal precoCusto;

//    @Column(name = "tempo_preparo")
//    private Integer tempoPreparo; // em minutos

//    @Column(name = "ingredientes", columnDefinition = "TEXT")
//    private String ingredientes;

    @Column(name = "imagem", columnDefinition = "TEXT")
    private String imagem;

    @Column(name = "destaque", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean destaque = false;

    @Column(name = "disponivel", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean disponivel = true;

    @Column(name = "ativo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @Column(name = "is_composto", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isComposto = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "produtoPai", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProdutoCompostoItem> itensComposto = new HashSet<>();

    @OneToMany(mappedBy = "produtoFilho", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProdutoCompostoItem> itensPai = new HashSet<>();
}