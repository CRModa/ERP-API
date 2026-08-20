package reset.reset.dto.restaurant;

import lombok.Data;
import reset.reset.Models.auth.User;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;
import reset.reset.Models.restaurant.ItemPedido;
import reset.reset.Models.restaurant.Mesa;
import reset.reset.Models.restaurant.Pedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PedidoRequest {

    private Long mesaId;
    private Long clienteId;
    private Long atendenteId;
    private Long garcomId;

//    @NotNull(message = "Tipo de pedido é obrigatório")
    private String tipo; // MESA, DELIVERY, TAKEAWAY

    private BigDecimal desconto;
    private BigDecimal taxaServico;
    private String observacao;

//    @NotNull(message = "Empresa ID é obrigatório")
    private Long empresaId;

//    @NotNull(message = "Itens do pedido são obrigatórios")
    private List<PedidoItemRequest> itens;

    public Pedido toEntity() {
        Pedido pedido = new Pedido();
        pedido.setTipo(Pedido.TipoPedido.valueOf(this.tipo));
        pedido.setDesconto(this.desconto);
        pedido.setTaxaServico(this.taxaServico);
        pedido.setObservacao(this.observacao);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        pedido.setEmpresa(empresa);

        if (this.mesaId != null) {
            Mesa mesa = new Mesa();
            mesa.setId(this.mesaId);
            pedido.setMesa(mesa);
        }

        if (this.clienteId != null) {
            Cliente cliente = new Cliente();
            cliente.setId(this.clienteId);
            pedido.setCliente(cliente);
        }

        if (this.atendenteId != null) {
            User atendente = new User();
            atendente.setId(this.atendenteId);
            pedido.setAtendente(atendente);
        }

        if (this.garcomId != null) {
            User garcom = new User();
            garcom.setId(this.garcomId);
            pedido.setGarcom(garcom);
        }

        if (this.itens != null) {
            pedido.setItens(this.itens.stream()
                    .map(itemRequest -> {
                        ItemPedido item = itemRequest.toEntity();
                        item.setPedido(pedido);
                        return item;
                    })
                    .collect(Collectors.toList()));
        }

        return pedido;
    }
}
