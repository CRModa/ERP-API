package reset.reset.dto.document.pdf;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpressaoRequest {

    @NotNull(message = "O ID do documento é obrigatório")
    private Long documentoId;

    @NotNull(message = "O ID do pedido é obrigatório")
    private Long pedidoId;

    private String tipoImpressao; // RECIBO, FATURA, ORCAMENTO
    private Integer copias = 1;
    private Boolean imprimirAutomatico = true;
    private String formato = "TERMICA"; // TERMICA, PDF
    private Boolean incluirTaxaServico = true;
    private Boolean incluirDesconto = true;

    public ImpressaoRequest(Long documentoId, Long pedidoId) {
        this.documentoId = documentoId;
        this.pedidoId = pedidoId;
        this.tipoImpressao = "RECIBO";
        this.copias = 1;
        this.imprimirAutomatico = true;
        this.formato = "TERMICA";
        this.incluirTaxaServico = true;
        this.incluirDesconto = true;
    }
}