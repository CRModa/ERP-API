package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Tipos.Recibo;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReciboDTO extends DocumentoDTO {
    private String formaPagamento;
    private LocalDateTime dataPagamento;
    private String referenciaPagamento;

    public static ReciboDTO fromEntity(Recibo recibo) {
        ReciboDTO dto = new ReciboDTO();
        dto.setId(recibo.getId());
        dto.setNumero(recibo.getNumero());
        dto.setData(recibo.getData());
        dto.setTotal(recibo.getTotal());
        dto.setEstado(recibo.getEstado());
        dto.setDataRegisto(recibo.getDataRegisto());
        dto.setEmpresaId(recibo.getEmpresa() != null ? recibo.getEmpresa().getId() : null);
        dto.setEmpresaNome(recibo.getEmpresa() != null ? recibo.getEmpresa().getNome() : null);
        dto.setClienteId(recibo.getCliente() != null ? recibo.getCliente().getId() : null);
        dto.setClienteNome(recibo.getCliente() != null ? recibo.getCliente().getNome() : null);
        dto.setTipoId(recibo.getTipo() != null ? recibo.getTipo().getId() : null);
        dto.setTipoDescricao(recibo.getTipo() != null ? recibo.getTipo().getDescricao() : null);
        dto.setFormaPagamento(recibo.getFormaPagamento());
        dto.setDataPagamento(recibo.getDataPagamento());
        dto.setReferenciaPagamento(recibo.getReferenciaPagamento());
        return dto;
    }
}