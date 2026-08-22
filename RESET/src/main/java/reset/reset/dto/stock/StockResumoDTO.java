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
public class StockResumoDTO {
    private Long produtoId;
    private String produtoNome;
    private String produtoCodigo;
    private Long armazemId;
    private String armazemNome;
    private BigDecimal quantidadeAtual;
    private BigDecimal precoVenda;
    private BigDecimal valorTotal;

    public static StockResumoDTO fromEntity(Stock stock) {
        return StockResumoDTO.builder()
                .produtoId(stock.getProduto().getId())
                .produtoNome(stock.getProduto().getNome())
                .produtoCodigo(stock.getProduto().getCodigo())
                .armazemId(stock.getArmazem().getId())
                .armazemNome(stock.getArmazem().getNome())
                .quantidadeAtual(stock.getQuantidadeAtual())
                .precoVenda(stock.getProduto().getPrecoVenda())
                .valorTotal(stock.getQuantidadeAtual().multiply(stock.getProduto().getPrecoVenda()))
                .build();
    }
}
