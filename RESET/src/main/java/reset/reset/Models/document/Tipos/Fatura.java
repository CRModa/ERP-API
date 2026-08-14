package reset.reset.Models.document.Tipos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Documento;

import java.time.LocalDate;

@Entity
@Table(name = "fatura")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Fatura extends Documento {

    @Column(name = "vencimento")
    private LocalDate vencimento;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean paga = false;
}
