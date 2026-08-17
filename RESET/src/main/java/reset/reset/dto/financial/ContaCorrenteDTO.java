package reset.reset.dto.financial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.financial.ContaCorrente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaCorrenteDTO {
    private Long id;
    private String tipoMovimento;
    private BigDecimal valor;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoAtual;
    private String descricao;
    private Boolean pago;
    private LocalDate dataMovimento;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private LocalDateTime createdAt;
    private Long empresaId;
    private String empresaNome;
    private Long clienteId;
    private String clienteNome;
    private Long fornecedorId;
    private String fornecedorNome;
    private Long documentoId;
    private String documentoNumero;

    public static ContaCorrenteDTO fromEntity(ContaCorrente conta) {
        return ContaCorrenteDTO.builder()
                .id(conta.getId())
                .tipoMovimento(conta.getTipoMovimento() != null ? conta.getTipoMovimento().name() : null)
                .valor(conta.getValor())
                .saldoAnterior(conta.getSaldoAnterior())
                .saldoAtual(conta.getSaldoAtual())
                .descricao(conta.getDescricao())
                .pago(conta.getPago())
                .dataMovimento(conta.getDataMovimento())
                .dataVencimento(conta.getDataVencimento())
                .dataPagamento(conta.getDataPagamento())
                .createdAt(conta.getCreatedAt())
                .empresaId(conta.getEmpresa() != null ? conta.getEmpresa().getId() : null)
                .empresaNome(conta.getEmpresa() != null ? conta.getEmpresa().getNome() : null)
                .clienteId(conta.getCliente() != null ? conta.getCliente().getId() : null)
                .clienteNome(conta.getCliente() != null ? conta.getCliente().getNome() : null)
                .fornecedorId(conta.getFornecedor() != null ? conta.getFornecedor().getId() : null)
                .fornecedorNome(conta.getFornecedor() != null ? conta.getFornecedor().getNome() : null)
                .documentoId(conta.getDocumento() != null ? conta.getDocumento().getId() : null)
                .documentoNumero(conta.getDocumento() != null ? conta.getDocumento().getNumero() : null)
                .build();
    }
}

