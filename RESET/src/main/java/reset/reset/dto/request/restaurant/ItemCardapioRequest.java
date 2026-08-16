package reset.reset.dto.request.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.Iva;
import reset.reset.Models.restaurant.CategoriaCardapio;
import reset.reset.Models.restaurant.ItemCardapio;

import java.math.BigDecimal;

@Data
public class ItemCardapioRequest {

    private String codigo;

    @NotBlank(message = "Nome do item é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser maior que zero")
    private BigDecimal preco;

    private BigDecimal custo;
    private Integer tempoPreparo;
    private String ingredientes;
    private String informacaoNutricional;
    private String imagem;
    private Boolean destaque;
    private Boolean disponivel;

    @NotNull(message = "Empresa ID é obrigatório")
    private Long empresaId;

    @NotNull(message = "Categoria ID é obrigatório")
    private Long categoriaId;

    private Long ivaId;

    public ItemCardapio toEntity() {
        ItemCardapio item = new ItemCardapio();
        item.setCodigo(this.codigo);
        item.setNome(this.nome);
        item.setDescricao(this.descricao);
        item.setPreco(this.preco);
        item.setCusto(this.custo);
        item.setTempoPreparo(this.tempoPreparo);
        item.setIngredientes(this.ingredientes);
        item.setInformacaoNutricional(this.informacaoNutricional);
        item.setImagem(this.imagem);
        item.setDestaque(this.destaque != null ? this.destaque : false);
        item.setDisponivel(this.disponivel != null ? this.disponivel : true);

        Empresa empresa = new Empresa();
        empresa.setId(this.empresaId);
        item.setEmpresa(empresa);

        CategoriaCardapio categoria = new CategoriaCardapio();
        categoria.setId(this.categoriaId);
        item.setCategoria(categoria);

        if (this.ivaId != null) {
            Iva iva = new Iva();
            iva.setId(this.ivaId);
            item.setIva(iva);
        }

        return item;
    }
}