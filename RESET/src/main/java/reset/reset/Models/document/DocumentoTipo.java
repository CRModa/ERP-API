package reset.reset.Models.document;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documento_tipo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoTipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String descricao;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ClasseDocumento classe;

    @Column(name = "serie_prefixo", length = 10)
    private String seriePrefixo;

    @Column(name = "numeracao_automatica")
    private Boolean numeracaoAutomatica = true;

    @Column(name = "movimenta_stock")
    private Boolean movimentaStock = false;

    @Column(name = "afeta_contas")
    private Boolean afetaContas = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    public enum ClasseDocumento {
        VENDA, COMPRA, FINANCEIRO, STOCK, OUTRO
    }
}
