package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Tipos.FaturaProforma;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaturaProformaDTO extends DocumentoDTO {
    private LocalDate vencimento;
    private Boolean convertidaEmFatura;
    private Long idFatura;

    public static FaturaProformaDTO fromEntity(FaturaProforma faturaProforma) {
        FaturaProformaDTO dto = new FaturaProformaDTO();
        dto.setId(faturaProforma.getId());
        dto.setNumero(faturaProforma.getNumero());
        dto.setData(faturaProforma.getData());
        dto.setTotal(faturaProforma.getTotal());
        dto.setEstado(faturaProforma.getEstado());
        dto.setDataRegisto(faturaProforma.getDataRegisto());
        dto.setEmpresaId(faturaProforma.getEmpresa() != null ? faturaProforma.getEmpresa().getId() : null);
        dto.setEmpresaNome(faturaProforma.getEmpresa() != null ? faturaProforma.getEmpresa().getNome() : null);
        dto.setClienteId(faturaProforma.getCliente() != null ? faturaProforma.getCliente().getId() : null);
        dto.setClienteNome(faturaProforma.getCliente() != null ? faturaProforma.getCliente().getNome() : null);
        dto.setTipoId(faturaProforma.getTipo() != null ? faturaProforma.getTipo().getId() : null);
        dto.setTipoDescricao(faturaProforma.getTipo() != null ? faturaProforma.getTipo().getDescricao() : null);
        dto.setVencimento(faturaProforma.getVencimento());
        dto.setConvertidaEmFatura(faturaProforma.getConvertidaEmFatura());
        dto.setIdFatura(faturaProforma.getIdFatura());
        return dto;
    }
}