package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.document.DocumentoTipo;

@Data
public class DocumentoTipoRequest {
    @NotBlank(message = "Descricao is required")
    @Size(max = 50, message = "Descricao must be less than 50 characters")
    private String descricao;

    @NotNull(message = "Classe is required")
    private String classe; // VENDA, COMPRA, FINANCEIRO, STOCK, OUTRO

    private String seriePrefixo;
    private Boolean numeracaoAutomatica;
    private Boolean movimentaStock;
    private Boolean afetaContas;

    public DocumentoTipo toEntity() {
        DocumentoTipo tipo = new DocumentoTipo();
        tipo.setDescricao(this.descricao);
        tipo.setClasse(DocumentoTipo.ClasseDocumento.valueOf(this.classe));
        tipo.setSeriePrefixo(this.seriePrefixo);
        tipo.setNumeracaoAutomatica(this.numeracaoAutomatica != null ? this.numeracaoAutomatica : true);
        tipo.setMovimentaStock(this.movimentaStock != null ? this.movimentaStock : false);
        tipo.setAfetaContas(this.afetaContas != null ? this.afetaContas : false);
        return tipo;
    }
}
