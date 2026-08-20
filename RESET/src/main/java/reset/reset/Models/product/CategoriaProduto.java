package reset.reset.Models.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.core.Empresa;

import java.util.List;

@Entity
@Table(name = "categoria_produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    private String codigo;

    private String descricao;

    private String observacao;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @Column(name = "visivel_restaurante", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean visivelRestaurante = false;

    @Column(name = "visivel_pos", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean visivelPos = false;

    @Column(name = "visivel_farmacia", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean visivelFarmacia = false;

    @Column(name = "visivel_web", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean visivelWeb = false;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)
    private List<Produto> produtos;
}