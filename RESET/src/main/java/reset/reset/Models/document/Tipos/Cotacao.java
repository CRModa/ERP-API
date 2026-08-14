package reset.reset.Models.document.Tipos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Documento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cotacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cotacao extends Documento {

    @Column(name = "valido_ate")
    private LocalDate validoAte;

    @Column(name = "taxa_conversao", precision = 10, scale = 4)
    private BigDecimal taxaConversao;

    @Column(name = "motivo_rejeicao", columnDefinition = "TEXT")
    private String motivoRejeicao;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;
}

