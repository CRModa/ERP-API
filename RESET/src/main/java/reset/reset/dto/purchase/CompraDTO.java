package reset.reset.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.purchase.Compra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraDTO {
    private Long id;
    private LocalDate data;
    private BigDecimal total;
    private String estado;
    private LocalDateTime dataRegisto;
    private Long empresaId;
    private String empresaNome;
    private Long fornecedorId;
    private String fornecedorNome;
    private String fornecedorNuit;
    private List<CompraItemDTO> itens;

    public static CompraDTO fromEntity(Compra compra) {
        return CompraDTO.builder()
                .id(compra.getId())
                .data(compra.getData())
                .total(compra.getTotal())
                .estado(compra.getEstado())
                .dataRegisto(compra.getDataRegisto())
                .empresaId(compra.getEmpresa() != null ? compra.getEmpresa().getId() : null)
                .empresaNome(compra.getEmpresa() != null ? compra.getEmpresa().getNome() : null)
                .fornecedorId(compra.getFornecedor() != null ? compra.getFornecedor().getId() : null)
                .fornecedorNome(compra.getFornecedor() != null ? compra.getFornecedor().getNome() : null)
                .fornecedorNuit(compra.getFornecedor() != null ? compra.getFornecedor().getNuit() : null)
                .itens(compra.getItens() != null ?
                        compra.getItens().stream()
                                .map(CompraItemDTO::fromEntity)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}

