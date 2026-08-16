package reset.reset.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;
import reset.reset.Models.customer.Fornecedor;
import reset.reset.Models.document.Documento;
import reset.reset.Models.financial.ContaCorrente;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContaCorrenteRequest {
    @NotNull(message = "Empresa ID is required")
    private Long empresaId;

    private Long clienteId;
    private Long fornecedorId;
    private Long documentoId;

    @NotNull(message = "Tipo movimento is required")
    private String tipoMovimento; // DEBITO, CREDITO

    @NotNull(message = "Valor is required")
    @Positive(message = "Valor must be greater than zero")
    private BigDecimal valor;

    private String descricao;
    private LocalDate dataVencimento;
    private LocalDate dataMovimento;

    public ContaCorrente toEntity() {
        ContaCorrente conta = new ContaCorrente();
        conta.setTipoMovimento(ContaCorrente.TipoMovimento.valueOf(this.tipoMovimento));
        conta.setValor(this.valor);
        conta.setDescricao(this.descricao);
        conta.setDataVencimento(this.dataVencimento);
        conta.setDataMovimento(this.dataMovimento != null ? this.dataMovimento : LocalDate.now());

        if (this.empresaId != null) {
            Empresa empresa = new Empresa();
            empresa.setId(this.empresaId);
            conta.setEmpresa(empresa);
        }

        if (this.clienteId != null) {
            Cliente cliente = new Cliente();
            cliente.setId(this.clienteId);
            conta.setCliente(cliente);
        }

        if (this.fornecedorId != null) {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setId(this.fornecedorId);
            conta.setFornecedor(fornecedor);
        }

        if (this.documentoId != null) {
            Documento documento = new Documento();
            documento.setId(this.documentoId);
            conta.setDocumento(documento);
        }

        return conta;
    }
}
