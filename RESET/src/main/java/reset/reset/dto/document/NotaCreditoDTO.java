package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Tipos.NotaCredito;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotaCreditoDTO extends DocumentoDTO {
    private Long documentoOrigemId;
    private String motivo;

    public static NotaCreditoDTO fromEntity(NotaCredito notaCredito) {
        NotaCreditoDTO dto = new NotaCreditoDTO();
        dto.setId(notaCredito.getId());
        dto.setNumero(notaCredito.getNumero());
        dto.setData(notaCredito.getData());
        dto.setTotal(notaCredito.getTotal());
        dto.setEstado(notaCredito.getEstado());
        dto.setDataRegisto(notaCredito.getDataRegisto());
        dto.setEmpresaId(notaCredito.getEmpresa() != null ? notaCredito.getEmpresa().getId() : null);
        dto.setEmpresaNome(notaCredito.getEmpresa() != null ? notaCredito.getEmpresa().getNome() : null);
        dto.setClienteId(notaCredito.getCliente() != null ? notaCredito.getCliente().getId() : null);
        dto.setClienteNome(notaCredito.getCliente() != null ? notaCredito.getCliente().getNome() : null);
        dto.setTipoId(notaCredito.getTipo() != null ? notaCredito.getTipo().getId() : null);
        dto.setTipoDescricao(notaCredito.getTipo() != null ? notaCredito.getTipo().getDescricao() : null);
        dto.setDocumentoOrigemId(notaCredito.getDocumentoOrigemId());
        dto.setMotivo(notaCredito.getMotivo());
        return dto;
    }
}