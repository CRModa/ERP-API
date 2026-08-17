package reset.reset.dto.accounting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.accounting.LancamentoContabil;
import reset.reset.Models.accounting.LancamentoContabilLinha;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoContabilDTO {
    private Long id;
    private String numeroLancamento;
    private LocalDate dataLancamento;
    private LocalDate dataValor;
    private String descricao;
    private LocalDateTime dataRegisto;
    private BigDecimal totalDebito;
    private BigDecimal totalCredito;
    private Long empresaId;
    private String empresaNome;
    private Long diarioId;
    private String diarioCodigo;
    private String diarioDescricao;
    private Long documentoId;
    private String documentoNumero;
    private Long utilizadorId;
    private String utilizadorNome;
    private List<LancamentoContabilLinhaDTO> linhas;

    public static LancamentoContabilDTO fromEntity(LancamentoContabil lancamento) {
        BigDecimal totalDebito = BigDecimal.ZERO;
        BigDecimal totalCredito = BigDecimal.ZERO;

        if (lancamento.getLinhas() != null) {
            for (var linha : lancamento.getLinhas()) {
                if (linha.getNatureza() == LancamentoContabilLinha.Natureza.D) {
                    totalDebito = totalDebito.add(linha.getValor());
                } else {
                    totalCredito = totalCredito.add(linha.getValor());
                }
            }
        }

        return LancamentoContabilDTO.builder()
                .id(lancamento.getId())
                .numeroLancamento(lancamento.getNumeroLancamento())
                .dataLancamento(lancamento.getDataLancamento())
                .dataValor(lancamento.getDataValor())
                .descricao(lancamento.getDescricao())
                .dataRegisto(lancamento.getDataRegisto())
                .totalDebito(totalDebito)
                .totalCredito(totalCredito)
                .empresaId(lancamento.getEmpresa() != null ? lancamento.getEmpresa().getId() : null)
                .empresaNome(lancamento.getEmpresa() != null ? lancamento.getEmpresa().getNome() : null)
                .diarioId(lancamento.getDiario() != null ? lancamento.getDiario().getId() : null)
                .diarioCodigo(lancamento.getDiario() != null ? lancamento.getDiario().getCodigo() : null)
                .diarioDescricao(lancamento.getDiario() != null ? lancamento.getDiario().getDescricao() : null)
                .documentoId(lancamento.getDocumento() != null ? lancamento.getDocumento().getId() : null)
                .documentoNumero(lancamento.getDocumento() != null ? lancamento.getDocumento().getNumero() : null)
                .utilizadorId(lancamento.getUtilizador() != null ? lancamento.getUtilizador().getId() : null)
                .utilizadorNome(lancamento.getUtilizador() != null ? lancamento.getUtilizador().getNome() : null)
                .linhas(lancamento.getLinhas() != null ?
                        lancamento.getLinhas().stream()
                                .map(LancamentoContabilLinhaDTO::fromEntity)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}
