package reset.reset.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.Desconto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescontoDTO {
    private Long id;
    private String descricao;
    private String tipo;
    private BigDecimal valor;
    private Boolean ativo;
    private LocalDateTime dataRegisto;
    private Long empresaId;
    private String empresaNome;

    public static DescontoDTO fromEntity(Desconto desconto) {
        return DescontoDTO.builder()
                .id(desconto.getId())
                .descricao(desconto.getDescricao())
                .tipo(desconto.getTipo())
                .valor(desconto.getValor())
                .ativo(desconto.isAtivo())
                .dataRegisto(desconto.getDataRegisto())
                .empresaId(desconto.getEmpresa() != null ? desconto.getEmpresa().getId() : null)
                .empresaNome(desconto.getEmpresa() != null ? desconto.getEmpresa().getNome() : null)
                .build();
    }
}
