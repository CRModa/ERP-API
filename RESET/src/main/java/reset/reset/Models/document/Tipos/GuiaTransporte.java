package reset.reset.Models.document.Tipos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Documento;

import java.time.LocalDateTime;

@Entity
@Table(name = "guia_transporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GuiaTransporte extends Documento {

    @Column(length = 50)
    private String matricula;

    @Column(length = 150)
    private String motorista;

    @Column(name = "data_carregamento")
    private LocalDateTime dataCarregamento;

    @Column(name = "data_descarga")
    private LocalDateTime dataDescarga;

    @Column(name = "observacoes_transporte", columnDefinition = "TEXT")
    private String observacoesTransporte;
}
