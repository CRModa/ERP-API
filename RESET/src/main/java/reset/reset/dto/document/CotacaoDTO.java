package reset.reset.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Tipos.Cotacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CotacaoDTO extends DocumentoDTO {
    private LocalDate validoAte;
    private BigDecimal taxaConversao;
    private String moeda;
    private LocalDateTime dataAprovacao;
    private String motivoRejeicao;
    private String tipoCotacao;

    public static CotacaoDTO fromEntity(Cotacao cotacao) {
        CotacaoDTO dto = new CotacaoDTO();
        dto.setId(cotacao.getId());
        dto.setNumero(cotacao.getNumero());
        dto.setData(cotacao.getData());
        dto.setTotal(cotacao.getTotal());
        dto.setEstado(cotacao.getEstado());
        dto.setDataRegisto(cotacao.getDataRegisto());
        dto.setEmpresaId(cotacao.getEmpresa() != null ? cotacao.getEmpresa().getId() : null);
        dto.setEmpresaNome(cotacao.getEmpresa() != null ? cotacao.getEmpresa().getNome() : null);
        dto.setClienteId(cotacao.getCliente() != null ? cotacao.getCliente().getId() : null);
        dto.setClienteNome(cotacao.getCliente() != null ? cotacao.getCliente().getNome() : null);
        dto.setTipoId(cotacao.getTipo() != null ? cotacao.getTipo().getId() : null);
        dto.setTipoDescricao(cotacao.getTipo() != null ? cotacao.getTipo().getDescricao() : null);
        dto.setValidoAte(cotacao.getValidoAte());
        dto.setTaxaConversao(cotacao.getTaxaConversao());
//        dto.setMoeda(cotacao.getMoeda());
        dto.setDataAprovacao(cotacao.getDataAprovacao());
        dto.setMotivoRejeicao(cotacao.getMotivoRejeicao());
//        dto.setTipoCotacao(cotacao.getTipoCotacao());
        return dto;
    }
}