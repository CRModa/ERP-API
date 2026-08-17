package reset.reset.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.Iva;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IvaDTO {
    private Long id;
    private String codigo;
    private String descricao;
    private BigDecimal taxa;
    private Boolean ativo;
    private LocalDateTime dataRegisto;
    private Long empresaId;
    private String empresaNome;

    public static IvaDTO fromEntity(Iva iva) {
        return IvaDTO.builder()
                .id(iva.getId())
                .codigo(iva.getCodigo())
                .descricao(iva.getDescricao())
                .taxa(iva.getTaxa())
                .ativo(iva.getAtivo())
                .dataRegisto(iva.getDataRegisto())
                .empresaId(iva.getEmpresa() != null ? iva.getEmpresa().getId() : null)
                .empresaNome(iva.getEmpresa() != null ? iva.getEmpresa().getNome() : null)
                .build();
    }
}
