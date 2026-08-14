package reset.reset.Models.financial;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.document.Documento;

import java.io.Serializable;

@Entity
@Table(name = "vd")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VD implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "venda_documento_id")
    private Documento vendaDocumento;

    @Id
    @ManyToOne
    @JoinColumn(name = "recibo_documento_id")
    private Documento reciboDocumento;
}
