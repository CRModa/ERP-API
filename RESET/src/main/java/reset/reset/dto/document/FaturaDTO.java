package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Tipos.Fatura;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaturaDTO extends DocumentoDTO {
    private LocalDate vencimento;
    private Boolean paga;
    private Long diasAtraso;

    public static FaturaDTO fromEntity(Fatura fatura) {
        FaturaDTO dto = new FaturaDTO();
        dto.setId(fatura.getId());
        dto.setNumero(fatura.getNumero());
        dto.setData(fatura.getData());
        dto.setTotal(fatura.getTotal());
        dto.setEstado(fatura.getEstado());
        dto.setDataRegisto(fatura.getDataRegisto());
        dto.setEmpresaId(fatura.getEmpresa() != null ? fatura.getEmpresa().getId() : null);
        dto.setEmpresaNome(fatura.getEmpresa() != null ? fatura.getEmpresa().getNome() : null);
        dto.setClienteId(fatura.getCliente() != null ? fatura.getCliente().getId() : null);
        dto.setClienteNome(fatura.getCliente() != null ? fatura.getCliente().getNome() : null);
        dto.setTipoId(fatura.getTipo() != null ? fatura.getTipo().getId() : null);
        dto.setTipoDescricao(fatura.getTipo() != null ? fatura.getTipo().getDescricao() : null);
        dto.setVencimento(fatura.getVencimento());
        dto.setPaga(fatura.getPaga());

        if (fatura.getVencimento() != null && !fatura.getPaga()) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(
                    fatura.getVencimento(), LocalDate.now());
            dto.setDiasAtraso(dias > 0 ? dias : 0);
        }

        return dto;
    }
}
