package reset.reset.Models.document.Tipos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Documento;

@Entity
@Table(name = "nota_debito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotaDebito extends Documento {

    @Column(name = "documento_origem_id")
    private Long documentoOrigemId;

    @Column(length = 100)
    private String motivo;
}
