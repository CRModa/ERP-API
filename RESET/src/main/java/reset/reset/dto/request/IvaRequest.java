package reset.reset.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.Iva;

import java.math.BigDecimal;

@Data
public class IvaRequest {
    @Size(max = 10, message = "Codigo must be less than 10 characters")
    private String codigo;

    @Size(max = 100, message = "Descricao must be less than 100 characters")
    private String descricao;

    @NotNull(message = "Taxa is required")
    private BigDecimal taxa;

    @NotNull(message = "Empresa ID is required")
    private Long empresaId;

    public Iva toEntity() {
        Iva iva = new Iva();
        iva.setCodigo(this.codigo);
        iva.setDescricao(this.descricao);
        iva.setTaxa(this.taxa);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        iva.setEmpresa(empresa);

        return iva;
    }
}
