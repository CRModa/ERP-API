package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Tipos.NotaDebito;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotaDebitoDTO extends DocumentoDTO {
    private Long documentoOrigemId;
    private String motivo;

    public static NotaDebitoDTO fromEntity(NotaDebito notaDebito) {
        NotaDebitoDTO dto = new NotaDebitoDTO();
        dto.setId(notaDebito.getId());
        dto.setNumero(notaDebito.getNumero());
        dto.setData(notaDebito.getData());
        dto.setTotal(notaDebito.getTotal());
        dto.setEstado(notaDebito.getEstado());
        dto.setDataRegisto(notaDebito.getDataRegisto());
        dto.setEmpresaId(notaDebito.getEmpresa() != null ? notaDebito.getEmpresa().getId() : null);
        dto.setEmpresaNome(notaDebito.getEmpresa() != null ? notaDebito.getEmpresa().getNome() : null);
        dto.setClienteId(notaDebito.getCliente() != null ? notaDebito.getCliente().getId() : null);
        dto.setClienteNome(notaDebito.getCliente() != null ? notaDebito.getCliente().getNome() : null);
        dto.setTipoId(notaDebito.getTipo() != null ? notaDebito.getTipo().getId() : null);
        dto.setTipoDescricao(notaDebito.getTipo() != null ? notaDebito.getTipo().getDescricao() : null);
        dto.setDocumentoOrigemId(notaDebito.getDocumentoOrigemId());
        dto.setMotivo(notaDebito.getMotivo());
        return dto;
    }
}