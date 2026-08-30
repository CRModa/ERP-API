package reset.reset.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoResponse {
    private Long id;
    private String numeroDocumento;
    private Long documentoId;
    private Long pedidoId;
    private String pedidoNumero;
    private Long contaId;
    private String contaNome;
    private Long clienteId;
    private String clienteNome;
    private BigDecimal valor;
    private String metodo;
    private BigDecimal troco;
    private String status;
    private LocalDateTime dataPagamento;
    private String observacao;
    private String tipoDocumento;
}
