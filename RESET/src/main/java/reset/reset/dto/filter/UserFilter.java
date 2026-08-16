package reset.reset.dto.filter;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFilter extends BaseFilter {
    private String username;
    private String nome;
    private String email;
    private String perfil;
    private Long empresaId;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}
