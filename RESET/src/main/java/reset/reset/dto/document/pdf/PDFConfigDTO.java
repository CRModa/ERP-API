package reset.reset.dto.document.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PDFConfigDTO {
    private String titulo;
    private String logoBase64;
    private String empresaNome;
    private String empresaNuit;
    private String empresaEndereco;
    private String empresaTelefone;
    private String empresaEmail;
    private String moeda;
    private String rodape;
    private boolean mostrarValores;
}

