package reset.reset.dto.financial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.financial.Conta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaDTO {
    private Long id;
    private String descricao;
    private String tipo;
    private LocalDateTime dataRegisto;
    private Long empresaId;
    private String empresaNome;
    private BigDecimal saldo;
    private boolean ativo;

    public static ContaDTO fromEntity(Conta conta) {
        return ContaDTO.builder()
                .id(conta.getId())
                .descricao(conta.getDescricao())
                .tipo(conta.getTipo())
                .dataRegisto(conta.getDataRegisto())
                .empresaId(conta.getEmpresa() != null ? conta.getEmpresa().getId() : null)
                .empresaNome(conta.getEmpresa() != null ? conta.getEmpresa().getNome() : null)
                .ativo(conta.getAtivo())
                .build();
    }
}
