package reset.reset.dto.request;

import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Fornecedor;
import reset.reset.Models.purchase.Compra;
import reset.reset.Models.purchase.CompraItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CompraRequest {
    private Long empresaId;
    private Long fornecedorId;
    private LocalDate data;
    private BigDecimal total;
    private String estado;
    private List<CompraItemRequest> itens;

    public Compra toEntity() {
        Compra compra = new Compra();
        compra.setData(this.data);
        compra.setTotal(this.total);
        compra.setEstado(this.estado);

        if (this.empresaId != null) {
            Empresa empresa = new Empresa();
            empresa.setId(this.empresaId);
            compra.setEmpresa(empresa);
        }

        if (this.fornecedorId != null) {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setId(this.fornecedorId);
            compra.setFornecedor(fornecedor);
        }

        if (this.itens != null) {
            compra.setItens(this.itens.stream()
                    .map(itemRequest -> {
                        CompraItem item = itemRequest.toEntity();
                        item.setCompra(compra);
                        return item;
                    })
                    .collect(Collectors.toList()));
        }

        return compra;
    }
}

