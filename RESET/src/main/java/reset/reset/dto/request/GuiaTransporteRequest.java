package reset.reset.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import reset.reset.Models.document.Tipos.GuiaTransporte;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class GuiaTransporteRequest extends DocumentoRequest {
    private String matricula;
    private String motorista;
    private LocalDateTime dataCarregamento;
    private LocalDateTime dataDescarga;
    private String observacoesTransporte;

    public GuiaTransporte toEntity() {
        GuiaTransporte guia = new GuiaTransporte();
        guia.setMatricula(this.matricula);
        guia.setMotorista(this.motorista);
        guia.setDataCarregamento(this.dataCarregamento);
        guia.setDataDescarga(this.dataDescarga);
        guia.setObservacoesTransporte(this.observacoesTransporte);
        return guia;
    }
}
