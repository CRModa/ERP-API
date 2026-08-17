package reset.reset.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.stock.Armazem;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArmazemDTO {
    private Long id;
    private String nome;
    private String localizacao;
    private LocalDateTime dataRegisto;
    private Long empresaId;
    private String empresaNome;
    private Long totalProdutos;

    public static ArmazemDTO fromEntity(Armazem armazem) {
        return ArmazemDTO.builder()
                .id(armazem.getId())
                .nome(armazem.getNome())
                .localizacao(armazem.getLocalizacao())
                .dataRegisto(armazem.getDataRegisto())
                .empresaId(armazem.getEmpresa() != null ? armazem.getEmpresa().getId() : null)
                .empresaNome(armazem.getEmpresa() != null ? armazem.getEmpresa().getNome() : null)
                .build();
    }
}

