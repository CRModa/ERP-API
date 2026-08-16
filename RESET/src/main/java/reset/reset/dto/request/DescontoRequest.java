package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.Desconto;

import java.math.BigDecimal;

@Data
public class DescontoRequest {
    @Size(max = 150, message = "Descricao must be less than 150 characters")
    private String descricao;

    @NotBlank(message = "Tipo is required")
    private String tipo; // PERCENTAGEM / VALOR

    @NotNull(message = "Valor is required")
    private BigDecimal valor;

    private Long empresaId;

    public Desconto toEntity() {
        Desconto desconto = new Desconto();
        desconto.setDescricao(this.descricao);
        desconto.setTipo(this.tipo);
        desconto.setValor(this.valor);

        if (this.empresaId != null) {
            Empresa empresa = new Empresa();
            empresa.setId(this.empresaId);
            desconto.setEmpresa(empresa);
        }

        return desconto;
    }
}
