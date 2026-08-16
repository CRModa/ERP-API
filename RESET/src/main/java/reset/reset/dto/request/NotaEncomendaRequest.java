package reset.reset.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import reset.reset.Models.document.Tipos.NotaEncomenda;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaEncomendaRequest extends DocumentoRequest {
    private Long cotacaoId;
    private LocalDate dataEntregaPrevista;
    private String transporte;
    private BigDecimal portes;

    public NotaEncomenda toEntity() {
        NotaEncomenda nota = new NotaEncomenda();
        nota.setCotacaoId(this.cotacaoId);
        nota.setDataEntregaPrevista(this.dataEntregaPrevista);
        nota.setTransporte(this.transporte);
        nota.setPortes(this.portes != null ? this.portes : BigDecimal.ZERO);
        return nota;
    }
}
