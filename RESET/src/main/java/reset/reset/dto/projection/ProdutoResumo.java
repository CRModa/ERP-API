package reset.reset.dto.projection;

import java.math.BigDecimal;

public interface ProdutoResumo {
    Long getId();

    String getCodigo();

    String getNome();

    BigDecimal getPrecoVenda();

    BigDecimal getPrecoCusto();

    String getCategoriaNome();
}
