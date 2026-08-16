package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;

import java.math.BigDecimal;

@Data
public class ClienteRequest {
    @NotBlank(message = "Nome is required")
    @Size(max = 200, message = "Nome must be less than 200 characters")
    private String nome;

    private String nuit;
    private String endereco;
    private String telefone;
    private String email;
    private String tipo;
    private BigDecimal descontoPadrao;
    private BigDecimal limiteCredito;
    private Long empresaId;

    public Cliente toEntity() {
        Cliente cliente = new Cliente();
        cliente.setNome(this.nome);
        cliente.setNuit(this.nuit);
        cliente.setEndereco(this.endereco);
        cliente.setTelefone(this.telefone);
        cliente.setEmail(this.email);
        cliente.setTipo(this.tipo);
        cliente.setDescontoPadrao(this.descontoPadrao != null ? this.descontoPadrao : BigDecimal.ZERO);
        cliente.setLimiteCredito(this.limiteCredito != null ? this.limiteCredito : BigDecimal.ZERO);

        if (this.empresaId != null) {
            Empresa empresa = new Empresa();
            empresa.setId(this.empresaId);
            cliente.setEmpresa(empresa);
        }

        return cliente;
    }
}
