package reset.reset.dto.request;

import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;
import reset.reset.Models.document.Documento;
import reset.reset.Models.document.DocumentoTipo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DocumentoRequest {
    private Long empresaId;
    private Long clienteId;
    private Long tipoId;
    private String numero;
    private LocalDate data;
    private BigDecimal total;
    private String estado;
    private List<DocumentoItemRequest> itens;

    public Documento toEntity() {
        Documento documento = new Documento();
        documento.setNumero(this.numero);
        documento.setData(this.data);
        documento.setTotal(this.total);
        documento.setEstado(this.estado);

        if (this.empresaId != null) {
            Empresa empresa = new Empresa();
            empresa.setId(this.empresaId);
            documento.setEmpresa(empresa);
        }

        if (this.clienteId != null) {
            Cliente cliente = new Cliente();
            cliente.setId(this.clienteId);
            documento.setCliente(cliente);
        }

        if (this.tipoId != null) {
            DocumentoTipo tipo = new DocumentoTipo();
            tipo.setId(this.tipoId);
            documento.setTipo(tipo);
        }

        return documento;
    }
}

