package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.core.Empresa;

@Data
public class EmpresaRequest {
    @NotBlank(message = "Nome is required")
    @Size(max = 200, message = "Nome must be less than 200 characters")
    private String nome;

    @NotBlank(message = "NUIT is required")
    @Size(max = 20, message = "NUIT must be less than 20 characters")
    private String nuit;

    private String endereco;
    private String telefone;
    private String email;
    private String moeda;
    private String pais;
    private String logotipo;

    public Empresa toEntity() {
        Empresa empresa = new Empresa();
        empresa.setNome(this.nome);
        empresa.setNuit(this.nuit);
        empresa.setEndereco(this.endereco);
        empresa.setTelefone(this.telefone);
        empresa.setEmail(this.email);
        empresa.setMoeda(this.moeda != null ? this.moeda : "MZN");
        empresa.setPais(this.pais != null ? this.pais : "Moçambique");
        empresa.setLogotipo(this.logotipo);
        return empresa;
    }
}

