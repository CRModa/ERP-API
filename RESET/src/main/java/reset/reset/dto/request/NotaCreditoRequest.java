package reset.reset.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import reset.reset.Models.document.Tipos.NotaCredito;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaCreditoRequest extends DocumentoRequest {
    private Long documentoOrigemId;
    private String motivo;

    public NotaCredito toEntity() {
        NotaCredito nota = new NotaCredito();
        nota.setDocumentoOrigemId(this.documentoOrigemId);
        nota.setMotivo(this.motivo);
        return nota;
    }
}
