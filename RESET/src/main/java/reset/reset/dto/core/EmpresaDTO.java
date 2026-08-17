package reset.reset.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.core.Empresa;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaDTO {
    private Long id;
    private String nome;
    private String nuit;
    private String endereco;
    private String telefone;
    private String email;
    private String moeda;
    private String pais;
    private Boolean ativo;
    private String logotipo;
    private LocalDateTime createdAt;
    private Long totalUtilizadores;
    private Long totalClientes;
    private Long totalProdutos;

    public static EmpresaDTO fromEntity(Empresa empresa) {
        return EmpresaDTO.builder()
                .id(empresa.getId())
                .nome(empresa.getNome())
                .nuit(empresa.getNuit())
                .endereco(empresa.getEndereco())
                .telefone(empresa.getTelefone())
                .email(empresa.getEmail())
                .moeda(empresa.getMoeda())
                .pais(empresa.getPais())
                .ativo(empresa.getAtivo())
                .logotipo(empresa.getLogotipo())
                .createdAt(empresa.getCreatedAt())
                .build();
    }
}

