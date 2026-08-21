package reset.reset.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.purchase.Compra;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraResumoDTO {
    private Long id;
    private LocalDate data;
    private BigDecimal total;
    private String estado;
    private String fornecedorNome;
    private Integer quantidadeItens;

    public static CompraResumoDTO fromEntity(Compra compra) {
        return CompraResumoDTO.builder()
                .id(compra.getId())
                .data(compra.getData())
                .total(compra.getTotal())
                .estado(compra.getEstado())
                .fornecedorNome(compra.getFornecedor() != null ?
                        compra.getFornecedor().getNome() : null)
                .quantidadeItens(compra.getItens() != null ?
                        compra.getItens().size() : 0)
                .build();
    }
}
