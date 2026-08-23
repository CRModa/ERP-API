package reset.reset.Models.restaurant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rest_combo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco", nullable = false, precision = 15, scale = 2)
    private BigDecimal preco;

    @Column(name = "desconto", precision = 15, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(name = "ativo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "combo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ItemCombo> itens = new ArrayList<>();

    // ========== MÉTODOS DE UTILIDADE ==========

    public BigDecimal calcularPrecoTotal() {
        if (this.itens == null || this.itens.isEmpty()) {
            return this.preco != null ? this.preco : BigDecimal.ZERO;
        }

        BigDecimal totalItens = this.itens.stream()
                .map(item -> {
                    Produto produto = item.getProduto();
                    if (produto != null && produto.getPrecoVenda() != null) {
                        return produto.getPrecoVenda().multiply(
                                item.getQuantidade() != null ? BigDecimal.valueOf(item.getQuantidade()) : BigDecimal.ONE
                        );
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Aplicar desconto do combo
        BigDecimal descontoValor = this.desconto != null ? this.desconto : BigDecimal.ZERO;
        return totalItens.subtract(descontoValor);
    }

    public boolean hasItens() {
        return this.itens != null && !this.itens.isEmpty();
    }
}