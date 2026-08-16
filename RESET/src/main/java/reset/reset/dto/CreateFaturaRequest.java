package reset.reset.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateFaturaRequest {
    private Long empresaId;
    private Long clienteId;
    private Long tipoId;
    private Long armazemId;
    private LocalDate data;
    private LocalDate vencimento;
    private List<ItemRequest> itens;

    @Data
    public static class ItemRequest {
        private Long produtoId;
        private BigDecimal quantidade;
        private BigDecimal precoUnitario;
        private Long ivaId;
        private Long descontoId;
    }
}

