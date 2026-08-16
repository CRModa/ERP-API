package reset.reset.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import reset.reset.Models.document.Tipos.Cotacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CotacaoRequest extends DocumentoRequest {
    private LocalDate validoAte;
    private BigDecimal taxaConversao;
    private String motivoRejeicao;
    private LocalDateTime dataAprovacao;

    public Cotacao toEntity() {
        Cotacao cotacao = new Cotacao();
        cotacao.setValidoAte(this.validoAte);
        cotacao.setTaxaConversao(this.taxaConversao);
        cotacao.setMotivoRejeicao(this.motivoRejeicao);
        cotacao.setDataAprovacao(this.dataAprovacao);
        return cotacao;
    }
}
