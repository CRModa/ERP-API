package reset.reset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import reset.reset.Models.accounting.Diario;
import reset.reset.Models.accounting.LancamentoContabil;
import reset.reset.Models.auth.User;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.document.Documento;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LancamentoContabilRequest {
    @NotNull(message = "Empresa ID is required")
    private Long empresaId;

    @NotNull(message = "Diario ID is required")
    private Long diarioId;

    @NotBlank(message = "Numero lancamento is required")
    @Size(max = 50, message = "Numero lancamento must be less than 50 characters")
    private String numeroLancamento;

    @NotNull(message = "Data lancamento is required")
    private LocalDate dataLancamento;

    private LocalDate dataValor;

    @NotBlank(message = "Descricao is required")
    @Size(max = 200, message = "Descricao must be less than 200 characters")
    private String descricao;

    private Long documentoId;
    private Long userId;

    @NotNull(message = "Linhas are required")
    private List<LancamentoContabilLinhaRequest> linhas;

    public LancamentoContabil toEntity() {
        LancamentoContabil lancamento = new LancamentoContabil();
        lancamento.setNumeroLancamento(this.numeroLancamento);
        lancamento.setDataLancamento(this.dataLancamento);
        lancamento.setDataValor(this.dataValor);
        lancamento.setDescricao(this.descricao);

        if (this.empresaId != null) {
            Empresa empresa = new Empresa();
            empresa.setId(this.empresaId);
            lancamento.setEmpresa(empresa);
        }

        if (this.diarioId != null) {
            Diario diario = new Diario();
            diario.setId(this.diarioId);
            lancamento.setDiario(diario);
        }

        if (this.documentoId != null) {
            Documento documento = new Documento();
            documento.setId(this.documentoId);
            lancamento.setDocumento(documento);
        }

        if (this.userId != null) {
            User user = new User();
            user.setId(this.userId);
            lancamento.setUtilizador(user);
        }

        if (this.linhas != null) {
            lancamento.setLinhas(this.linhas.stream()
                    .map(LancamentoContabilLinhaRequest::toEntity)
                    .collect(Collectors.toList()));
        }

        return lancamento;
    }
}
