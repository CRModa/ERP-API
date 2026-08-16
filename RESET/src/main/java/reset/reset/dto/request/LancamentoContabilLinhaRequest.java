package reset.reset.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import reset.reset.Models.accounting.ContaContabil;
import reset.reset.Models.accounting.LancamentoContabilLinha;

import java.math.BigDecimal;

@Data
public class LancamentoContabilLinhaRequest {
    @NotNull(message = "Conta contabil ID is required")
    private Long contaContabilId;

    @NotNull(message = "Valor is required")
    @Positive(message = "Valor must be greater than zero")
    private BigDecimal valor;

    @NotNull(message = "Natureza is required")
    private String natureza; // D, C

    private String descricao;

    public LancamentoContabilLinha toEntity() {
        LancamentoContabilLinha linha = new LancamentoContabilLinha();
        linha.setValor(this.valor);
        linha.setNatureza(LancamentoContabilLinha.Natureza.valueOf(this.natureza));
        linha.setDescricao(this.descricao);

        if (this.contaContabilId != null) {
            ContaContabil conta = new ContaContabil();
            conta.setId(this.contaContabilId);
            linha.setContaContabil(conta);
        }

        return linha;
    }
}
