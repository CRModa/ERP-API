package reset.reset.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.customer.Cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private Long id;
    private String nome;
    private String nuit;
    private String endereco;
    private String telefone;
    private String email;
    private String tipo;
    private BigDecimal descontoPadrao;
    private BigDecimal limiteCredito;
    private BigDecimal saldoCorrente;
    private Boolean ativo;
    private LocalDateTime dataRegisto;
    private Long empresaId;
    private String empresaNome;

    public static ClienteDTO fromEntity(Cliente cliente) {
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .nuit(cliente.getNuit())
                .endereco(cliente.getEndereco())
                .telefone(cliente.getTelefone())
                .email(cliente.getEmail())
                .tipo(cliente.getTipo())
                .descontoPadrao(cliente.getDescontoPadrao())
                .limiteCredito(cliente.getLimiteCredito())
                .saldoCorrente(cliente.getSaldoCorrente())
                .ativo(cliente.getAtivo())
                .dataRegisto(cliente.getDataRegisto())
                .empresaId(cliente.getEmpresa() != null ? cliente.getEmpresa().getId() : null)
                .empresaNome(cliente.getEmpresa() != null ? cliente.getEmpresa().getNome() : null)
                .build();
    }
}

