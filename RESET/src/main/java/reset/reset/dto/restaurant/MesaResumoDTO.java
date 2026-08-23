package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.restaurant.Mesa;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesaResumoDTO {
    private Long id;
    private String numero;
    private Integer capacidade;
    private String status;
    private String localizacao;
    private Integer pedidosAtivos;

    public static MesaResumoDTO fromEntity(Mesa mesa) {
        if (mesa == null) return null;

        return MesaResumoDTO.builder()
                .id(mesa.getId())
                .numero(mesa.getNumero())
                .capacidade(mesa.getCapacidade())
                .status(mesa.getStatus() != null ? mesa.getStatus().name() : null)
                .localizacao(mesa.getLocalizacao())
                .build();
    }
}
