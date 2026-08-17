package reset.reset.dto.accounting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.accounting.ContaContabil;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaContabilDTO {
    private Long id;
    private String codigo;
    private String descricao;
    private String tipo;
    private Long empresaId;
    private String empresaNome;
    private BigDecimal saldo;

    public static ContaContabilDTO fromEntity(ContaContabil conta) {
        return ContaContabilDTO.builder()
                .id(conta.getId())
                .codigo(conta.getCodigo())
                .descricao(conta.getDescricao())
                .tipo(conta.getTipo())
                .empresaId(conta.getEmpresa() != null ? conta.getEmpresa().getId() : null)
                .empresaNome(conta.getEmpresa() != null ? conta.getEmpresa().getNome() : null)
                .build();
    }
}

