package reset.reset.dto.accounting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.accounting.LancamentoContabilLinha;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoContabilLinhaDTO {
    private Long id;
    private BigDecimal valor;
    private String natureza;
    private String descricao;
    private Long contaContabilId;
    private String contaContabilCodigo;
    private String contaContabilDescricao;

    public static LancamentoContabilLinhaDTO fromEntity(LancamentoContabilLinha linha) {
        return LancamentoContabilLinhaDTO.builder()
                .id(linha.getId())
                .valor(linha.getValor())
                .natureza(linha.getNatureza() != null ? linha.getNatureza().name() : null)
                .descricao(linha.getDescricao())
                .contaContabilId(linha.getContaContabil() != null ? linha.getContaContabil().getId() : null)
                .contaContabilCodigo(linha.getContaContabil() != null ? linha.getContaContabil().getCodigo() : null)
                .contaContabilDescricao(linha.getContaContabil() != null ? linha.getContaContabil().getDescricao() : null)
                .build();
    }
}
