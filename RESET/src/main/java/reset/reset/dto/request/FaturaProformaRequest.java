package reset.reset.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import reset.reset.Models.document.Tipos.FaturaProforma;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class FaturaProformaRequest extends DocumentoRequest {
    private LocalDate vencimento;

    public FaturaProforma toEntity() {
        FaturaProforma fatura = new FaturaProforma();
        fatura.setVencimento(this.vencimento);
        return fatura;
    }
}
