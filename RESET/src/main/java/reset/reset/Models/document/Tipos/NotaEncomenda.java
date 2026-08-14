package reset.reset.Models.document.Tipos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Documento;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "nota_encomenda")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotaEncomenda extends Documento {

    @Column(name = "cotacao_id", nullable = false)
    private Long cotacaoId;

    @Column(name = "data_entrega_prevista")
    private LocalDate dataEntregaPrevista;

    @Column(length = 100)
    private String transporte;

    @Column(precision = 10, scale = 2)
    private BigDecimal portes = BigDecimal.ZERO;
}
