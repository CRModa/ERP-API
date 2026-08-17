package reset.reset.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.customer.Fornecedor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FornecedorDTO {
    private Long id;
    private String nome;
    private String nuit;
    private String endereco;
    private String telefone;
    private String email;
    private Boolean ativo;
    private LocalDateTime dataRegisto;
    private Long empresaId;
    private String empresaNome;

    public static FornecedorDTO fromEntity(Fornecedor fornecedor) {
        return FornecedorDTO.builder()
                .id(fornecedor.getId())
                .nome(fornecedor.getNome())
                .nuit(fornecedor.getNuit())
                .endereco(fornecedor.getEndereco())
                .telefone(fornecedor.getTelefone())
                .email(fornecedor.getEmail())
                .ativo(fornecedor.getAtivo())
                .dataRegisto(fornecedor.getDataRegisto())
                .empresaId(fornecedor.getEmpresa() != null ? fornecedor.getEmpresa().getId() : null)
                .empresaNome(fornecedor.getEmpresa() != null ? fornecedor.getEmpresa().getNome() : null)
                .build();
    }
}
