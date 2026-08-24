package reset.reset.Models.stock;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimento_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "armazem_id")
    private Armazem armazem;

    @Column(length = 30)
    private String tipo; // ENTRADA_COMPRA, SAIDA_VENDA, AJUSTE, DEVOLUCAO

    @Column(precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(length = 100)
    private String referencia;

    private String observacao;

    @CreationTimestamp
    @Column(name = "data_movimento", updatable = false)
    private LocalDateTime dataMovimento;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;
}
