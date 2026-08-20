package reset.reset.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProdutoCompostoRequest extends ProdutoRequest {

    private Boolean isComposto = false;
    private Integer tempoPreparo;
    private String ingredientes;
    private String imagem;
    private Boolean destaque = false;
    private Boolean disponivel = true;

    private List<ProdutoCompostoItemRequest> itensComposto;
}

