package reset.reset.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Fornecedor;

@Data
public class FornecedorRequest {
    @Size(max = 200, message = "Nome must be less than 200 characters")
    private String nome;

    private String nuit;
    private String endereco;
    private String telefone;
    private String email;
    private Long empresaId;

    public Fornecedor toEntity() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(this.nome);
        fornecedor.setNuit(this.nuit);
        fornecedor.setEndereco(this.endereco);
        fornecedor.setTelefone(this.telefone);
        fornecedor.setEmail(this.email);

        if (this.empresaId != null) {
            Empresa empresa = new Empresa();
            empresa.setId(this.empresaId);
            fornecedor.setEmpresa(empresa);
        }

        return fornecedor;
    }
}
