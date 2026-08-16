package reset.reset.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MesaEstatisticas {
    private Long total;
    private Long ocupadas;
    private Long disponiveis;
    private Long reservadas;
    private Long emLimpeza;
}