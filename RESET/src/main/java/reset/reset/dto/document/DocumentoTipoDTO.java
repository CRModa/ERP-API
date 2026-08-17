package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.DocumentoTipo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoTipoDTO {
    private Long id;
    private String descricao;
    private String classe;
    private String seriePrefixo;
    private Boolean numeracaoAutomatica;
    private Boolean movimentaStock;
    private Boolean afetaContas;

    public static DocumentoTipoDTO fromEntity(DocumentoTipo tipo) {
        return DocumentoTipoDTO.builder()
                .id(tipo.getId())
                .descricao(tipo.getDescricao())
                .classe(tipo.getClasse() != null ? tipo.getClasse().name() : null)
                .seriePrefixo(tipo.getSeriePrefixo())
                .numeracaoAutomatica(tipo.getNumeracaoAutomatica())
                .movimentaStock(tipo.getMovimentaStock())
                .afetaContas(tipo.getAfetaContas())
                .build();
    }
}
