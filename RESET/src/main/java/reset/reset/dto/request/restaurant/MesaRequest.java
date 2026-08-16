package reset.reset.dto.request.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.restaurant.Mesa;

@Data
public class MesaRequest {

    @NotBlank(message = "Número da mesa é obrigatório")
    private String numero;

    @Positive(message = "Capacidade deve ser maior que zero")
    private Integer capacidade = 4;

    private String localizacao;

    @NotNull(message = "Empresa ID é obrigatório")
    private Long empresaId;

    public Mesa toEntity() {
        Mesa mesa = new Mesa();
        mesa.setNumero(this.numero);
        mesa.setCapacidade(this.capacidade);
        mesa.setLocalizacao(this.localizacao);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        mesa.setEmpresa(empresa);

        return mesa;
    }
}
