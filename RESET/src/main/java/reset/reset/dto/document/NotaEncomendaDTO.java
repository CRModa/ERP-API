package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Tipos.NotaEncomenda;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotaEncomendaDTO extends DocumentoDTO {
    private Long cotacaoId;
    private LocalDate dataEntregaPrevista;
    private BigDecimal portes;

    public static NotaEncomendaDTO fromEntity(NotaEncomenda notaEncomenda) {
        NotaEncomendaDTO dto = new NotaEncomendaDTO();
        dto.setId(notaEncomenda.getId());
        dto.setNumero(notaEncomenda.getNumero());
        dto.setData(notaEncomenda.getData());
        dto.setTotal(notaEncomenda.getTotal());
        dto.setEstado(notaEncomenda.getEstado());
        dto.setDataRegisto(notaEncomenda.getDataRegisto());
        dto.setEmpresaId(notaEncomenda.getEmpresa() != null ? notaEncomenda.getEmpresa().getId() : null);
        dto.setEmpresaNome(notaEncomenda.getEmpresa() != null ? notaEncomenda.getEmpresa().getNome() : null);
        dto.setClienteId(notaEncomenda.getCliente() != null ? notaEncomenda.getCliente().getId() : null);
        dto.setClienteNome(notaEncomenda.getCliente() != null ? notaEncomenda.getCliente().getNome() : null);
        dto.setTipoId(notaEncomenda.getTipo() != null ? notaEncomenda.getTipo().getId() : null);
        dto.setTipoDescricao(notaEncomenda.getTipo() != null ? notaEncomenda.getTipo().getDescricao() : null);
        dto.setCotacaoId(notaEncomenda.getCotacaoId());
        dto.setDataEntregaPrevista(notaEncomenda.getDataEntregaPrevista());
        dto.setPortes(notaEncomenda.getPortes());
        return dto;
    }
}