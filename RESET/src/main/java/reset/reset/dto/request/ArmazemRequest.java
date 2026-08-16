package reset.reset.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.stock.Armazem;

@Data
public class ArmazemRequest {
    @NotBlank(message = "Nome is required")
    @Size(max = 100, message = "Nome must be less than 100 characters")
    private String nome;

    private String localizacao;

    @NotNull(message = "Empresa ID is required")
    private Long empresaId;

    public Armazem toEntity() {
        Armazem armazem = new Armazem();
        armazem.setNome(this.nome);
        armazem.setLocalizacao(this.localizacao);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        armazem.setEmpresa(empresa);

        return armazem;
    }
}

