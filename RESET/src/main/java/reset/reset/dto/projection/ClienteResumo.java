package reset.reset.dto.projection;

import java.math.BigDecimal;

public interface ClienteResumo {
    Long getId();
    String getNome();
    String getNuit();
    String getTelefone();
    String getEmail();
    BigDecimal getSaldoCorrente();
}

