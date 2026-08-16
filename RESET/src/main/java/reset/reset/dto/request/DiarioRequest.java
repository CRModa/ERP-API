package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.accounting.Diario;
import reset.reset.Models.core.Empresa;

@Data
public class DiarioRequest {
    @NotBlank(message = "Codigo is required")
    @Size(max = 20, message = "Codigo must be less than 20 characters")
    private String codigo;

    @Size(max = 100, message = "Descricao must be less than 100 characters")
    private String descricao;

    @NotNull(message = "Empresa ID is required")
    private Long empresaId;

    public Diario toEntity() {
        Diario diario = new Diario();
        diario.setCodigo(this.codigo);
        diario.setDescricao(this.descricao);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        diario.setEmpresa(empresa);

        return diario;
    }
}
