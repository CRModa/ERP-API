package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.accounting.ContaContabil;
import reset.reset.Models.core.Empresa;

@Data
public class ContaContabilRequest {
    @NotBlank(message = "Codigo is required")
    @Size(max = 20, message = "Codigo must be less than 20 characters")
    private String codigo;

    @NotBlank(message = "Descricao is required")
    @Size(max = 150, message = "Descricao must be less than 150 characters")
    private String descricao;

    @NotBlank(message = "Tipo is required")
    private String tipo; // ATIVO, PASSIVO, CUSTO, RENDIMENTO

    @NotNull(message = "Empresa ID is required")
    private Long empresaId;

    public ContaContabil toEntity() {
        ContaContabil conta = new ContaContabil();
        conta.setCodigo(this.codigo);
        conta.setDescricao(this.descricao);
        conta.setTipo(this.tipo);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        conta.setEmpresa(empresa);

        return conta;
    }
}

