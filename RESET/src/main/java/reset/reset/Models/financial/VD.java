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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venda_documento_id")
    private Documento vendaDocumento;

    @ManyToOne
    @JoinColumn(name = "recibo_documento_id")
    private Documento reciboDocumento;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;
}
