package reset.reset.dto.financial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.financial.ContaCorrente;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaCorrenteResumoDTO {
    private Long id;
    private String tipoMovimento;
    private BigDecimal valor;
    private String descricao;
    private Boolean pago;
    private LocalDate dataMovimento;
    private LocalDate dataVencimento;
    private String clienteNome;
    private String fornecedorNome;

    public static ContaCorrenteResumoDTO fromEntity(ContaCorrente conta) {
        return ContaCorrenteResumoDTO.builder()
                .id(conta.getId())
                .tipoMovimento(conta.getTipoMovimento() != null ? conta.getTipoMovimento().name() : null)
                .valor(conta.getValor())
                .descricao(conta.getDescricao())
                .pago(conta.getPago())
                .dataMovimento(conta.getDataMovimento())
                .dataVencimento(conta.getDataVencimento())
                .clienteNome(conta.getCliente() != null ? conta.getCliente().getNome() : null)
                .fornecedorNome(conta.getFornecedor() != null ? conta.getFornecedor().getNome() : null)
                .build();
    }
}