package reset.reset.dto.filter;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmpresaFilter extends BaseFilter {
    private String nome;
    private String nuit;
    private String email;
    private String telefone;
    private String pais;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}
