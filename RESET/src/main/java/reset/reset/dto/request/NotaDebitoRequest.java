package reset.reset.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import reset.reset.Models.document.Tipos.NotaDebito;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaDebitoRequest extends DocumentoRequest {
    private Long documentoOrigemId;
    private String motivo;

    public NotaDebito toEntity() {
        NotaDebito nota = new NotaDebito();
        nota.setDocumentoOrigemId(this.documentoOrigemId);
        nota.setMotivo(this.motivo);
        return nota;
    }
}
