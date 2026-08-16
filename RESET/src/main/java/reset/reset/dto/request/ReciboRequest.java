package reset.reset.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import reset.reset.Models.document.Tipos.Recibo;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReciboRequest extends DocumentoRequest {
    private String formaPagamento;
    private String referenciaPagamento;
    private LocalDateTime dataPagamento;

    public Recibo toEntity() {
        Recibo recibo = new Recibo();
        recibo.setFormaPagamento(this.formaPagamento);
        recibo.setReferenciaPagamento(this.referenciaPagamento);
        recibo.setDataPagamento(this.dataPagamento);
        return recibo;
    }
}
