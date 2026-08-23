package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.restaurant.Mesa;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesaDTO {
    private Long id;
    private String numero;
    private Integer capacidade;
    private String localizacao;
    private String status;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private Long empresaId;
    private String empresaNome;
    private Integer pedidosAtivos;

    public static MesaDTO fromEntity(Mesa mesa) {
        if (mesa == null) return null;

        return MesaDTO.builder()
                .id(mesa.getId())
                .numero(mesa.getNumero())
                .capacidade(mesa.getCapacidade())
                .localizacao(mesa.getLocalizacao())
                .status(mesa.getStatus() != null ? mesa.getStatus().name() : null)
                .ativo(mesa.getAtivo())
                .createdAt(mesa.getCreatedAt())
                .empresaId(mesa.getEmpresa() != null ? mesa.getEmpresa().getId() : null)
                .empresaNome(mesa.getEmpresa() != null ? mesa.getEmpresa().getNome() : null)
                .build();
    }

    public static MesaDTO fromEntityWithPedidos(Mesa mesa, Integer pedidosAtivos) {
        MesaDTO dto = fromEntity(mesa);
        dto.setPedidosAtivos(pedidosAtivos);
        return dto;
    }
}

