package reset.reset.dto.projection;

import java.math.BigDecimal;

public interface StockResumo {
    Long getProdutoId();

    String getProdutoNome();

    String getProdutoCodigo();

    Long getArmazemId();

    String getArmazemNome();

    BigDecimal getQuantidadeAtual();

    BigDecimal getPrecoVenda();
}
