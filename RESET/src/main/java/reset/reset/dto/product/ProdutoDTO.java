package reset.reset.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.product.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDTO {
    private Long id;
    private String codigo;
    private String nome;
    private String descricao;
    private BigDecimal precoVenda;
    private BigDecimal precoCusto;
    private Boolean ativo;
    private LocalDateTime dataRegisto;
    private Long empresaId;
    private String empresaNome;
    private Long categoriaId;
    private String categoriaNome;
    private String categoriaCodigo;
    private Long ivaId;
    private String ivaCodigo;
    private BigDecimal ivaTaxa;
    private BigDecimal quantidadeEstoque;

    public static ProdutoDTO fromEntity(Produto produto) {
        return ProdutoDTO.builder()
                .id(produto.getId())
                .codigo(produto.getCodigo())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .precoVenda(produto.getPrecoVenda())
                .precoCusto(produto.getPrecoCusto())
                .ativo(produto.getAtivo())
                .dataRegisto(produto.getDataRegisto())
                .empresaId(produto.getEmpresa() != null ? produto.getEmpresa().getId() : null)
                .empresaNome(produto.getEmpresa() != null ? produto.getEmpresa().getNome() : null)
                .categoriaId(produto.getCategoria() != null ? produto.getCategoria().getId() : null)
                .categoriaNome(produto.getCategoria() != null ? produto.getCategoria().getDescricao() : null)
                .categoriaCodigo(produto.getCategoria() != null ? produto.getCategoria().getCodigo() : null)
                .ivaId(produto.getIva() != null ? produto.getIva().getId() : null)
                .ivaCodigo(produto.getIva() != null ? produto.getIva().getCodigo() : null)
                .ivaTaxa(produto.getIva() != null ? produto.getIva().getTaxa() : null)
                .build();
    }
}
