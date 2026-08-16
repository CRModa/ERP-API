package reset.reset.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DocumentoResumo {
    Long getId();

    String getNumero();

    LocalDate getData();

    BigDecimal getTotal();

    String getEstado();

    String getClienteNome();

    String getTipoDescricao();
}
