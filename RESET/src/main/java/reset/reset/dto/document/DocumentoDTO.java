package reset.reset.dto.document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Documento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDTO {
    private Long id;
    private String numero;
    private LocalDate data;
    private BigDecimal total;
    private String estado;
    private LocalDateTime dataRegisto;
    private Long empresaId;
    private String empresaNome;
    private Long clienteId;
    private String clienteNome;
    private String clienteNuit;
    private Long tipoId;
    private String tipoDescricao;
    private String tipoClasse;
    private List<DocumentoItemDTO> itens;

    public static DocumentoDTO fromEntity(Documento documento) {
        return DocumentoDTO.builder()
                .id(documento.getId())
                .numero(documento.getNumero())
                .data(documento.getData())
                .total(documento.getTotal())
                .estado(documento.getEstado())
                .dataRegisto(documento.getDataRegisto())
                .empresaId(documento.getEmpresa() != null ? documento.getEmpresa().getId() : null)
                .empresaNome(documento.getEmpresa() != null ? documento.getEmpresa().getNome() : null)
                .clienteId(documento.getCliente() != null ? documento.getCliente().getId() : null)
                .clienteNome(documento.getCliente() != null ? documento.getCliente().getNome() : null)
                .clienteNuit(documento.getCliente() != null ? documento.getCliente().getNuit() : null)
                .tipoId(documento.getTipo() != null ? documento.getTipo().getId() : null)
                .tipoDescricao(documento.getTipo() != null ? documento.getTipo().getDescricao() : null)
                .tipoClasse(documento.getTipo() != null && documento.getTipo().getClasse() != null ?
                        documento.getTipo().getClasse().name() : null)
                .itens(documento.getItens() != null ?
                        documento.getItens().stream()
                                .map(DocumentoItemDTO::fromEntity)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}

