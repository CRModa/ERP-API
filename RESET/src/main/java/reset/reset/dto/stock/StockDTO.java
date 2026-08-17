package reset.reset.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.stock.Stock;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {
    private Long id;
    private BigDecimal quantidadeAtual;
    private Long produtoId;
    private String produtoNome;
    private String produtoCodigo;
    private BigDecimal precoVenda;
    private BigDecimal precoCusto;
    private Long armazemId;
    private String armazemNome;
    private Long empresaId;
    private String empresaNome;

    public static StockDTO fromEntity(Stock stock) {
        return StockDTO.builder()
                .id(stock.getId())
                .quantidadeAtual(stock.getQuantidadeAtual())
                .produtoId(stock.getProduto() != null ? stock.getProduto().getId() : null)
                .produtoNome(stock.getProduto() != null ? stock.getProduto().getNome() : null)
                .produtoCodigo(stock.getProduto() != null ? stock.getProduto().getCodigo() : null)
                .precoVenda(stock.getProduto() != null ? stock.getProduto().getPrecoVenda() : null)
                .precoCusto(stock.getProduto() != null ? stock.getProduto().getPrecoCusto() : null)
                .armazemId(stock.getArmazem() != null ? stock.getArmazem().getId() : null)
                .armazemNome(stock.getArmazem() != null ? stock.getArmazem().getNome() : null)
                .empresaId(stock.getProduto() != null && stock.getProduto().getEmpresa() != null ?
                        stock.getProduto().getEmpresa().getId() : null)
                .empresaNome(stock.getProduto() != null && stock.getProduto().getEmpresa() != null ?
                        stock.getProduto().getEmpresa().getNome() : null)
                .build();
    }
}
