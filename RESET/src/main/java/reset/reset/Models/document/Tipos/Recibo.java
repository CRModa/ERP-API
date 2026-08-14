package reset.reset.Models.document.Tipos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Documento;

import java.time.LocalDateTime;

@Entity
@Table(name = "recibo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Recibo extends Documento {

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Column(name = "referencia_pagamento", length = 50)
    private String referenciaPagamento;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;
}
