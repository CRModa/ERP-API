package reset.reset.dto.restaurant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoRequest {

    @NotNull(message = "ID do pedido é obrigatório")
    private Long pedidoId;

    @NotNull(message = "Método de pagamento é obrigatório")
    private String metodo; // DINHEIRO, CARTAO, TRANSFERENCIA, M-PESA

    private BigDecimal valorRecebido;
    private BigDecimal troco;

    @NotNull(message = "Conta de origem é obrigatória")
    private Long contaId;

    private Long clienteId;
    private String observacao;
}

