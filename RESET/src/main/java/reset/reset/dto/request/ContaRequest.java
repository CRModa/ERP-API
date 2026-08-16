package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.financial.Conta;

@Data
public class ContaRequest {
    @NotBlank(message = "Descricao is required")
    @Size(max = 100, message = "Descricao must be less than 100 characters")
    private String descricao;

    @NotBlank(message = "Tipo is required")
    private String tipo; // CAIXA, BANCO

    @NotNull(message = "Empresa ID is required")
    private Long empresaId;

    public Conta toEntity() {
        Conta conta = new Conta();
        conta.setDescricao(this.descricao);
        conta.setTipo(this.tipo);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        conta.setEmpresa(empresa);

        return conta;
    }
}

