package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Tipos.GuiaTransporte;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiaTransporteDTO extends DocumentoDTO {
    private String matricula;
    private String motorista;
    private LocalDateTime dataCarregamento;
    private LocalDateTime dataDescarga;
    private String observacoesTransporte;

    public static GuiaTransporteDTO fromEntity(GuiaTransporte guiaTransporte) {
        GuiaTransporteDTO dto = new GuiaTransporteDTO();
        dto.setId(guiaTransporte.getId());
        dto.setNumero(guiaTransporte.getNumero());
        dto.setData(guiaTransporte.getData());
        dto.setTotal(guiaTransporte.getTotal());
        dto.setEstado(guiaTransporte.getEstado());
        dto.setDataRegisto(guiaTransporte.getDataRegisto());
        dto.setEmpresaId(guiaTransporte.getEmpresa() != null ? guiaTransporte.getEmpresa().getId() : null);
        dto.setEmpresaNome(guiaTransporte.getEmpresa() != null ? guiaTransporte.getEmpresa().getNome() : null);
        dto.setClienteId(guiaTransporte.getCliente() != null ? guiaTransporte.getCliente().getId() : null);
        dto.setClienteNome(guiaTransporte.getCliente() != null ? guiaTransporte.getCliente().getNome() : null);
        dto.setTipoId(guiaTransporte.getTipo() != null ? guiaTransporte.getTipo().getId() : null);
        dto.setTipoDescricao(guiaTransporte.getTipo() != null ? guiaTransporte.getTipo().getDescricao() : null);
        dto.setMatricula(guiaTransporte.getMatricula());
        dto.setMotorista(guiaTransporte.getMotorista());
        dto.setDataCarregamento(guiaTransporte.getDataCarregamento());
        dto.setDataDescarga(guiaTransporte.getDataDescarga());
        dto.setObservacoesTransporte(guiaTransporte.getObservacoesTransporte());
        return dto;
    }
}