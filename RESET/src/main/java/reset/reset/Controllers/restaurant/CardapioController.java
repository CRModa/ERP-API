package reset.reset.Controllers.restaurant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Controllers.base.BaseController;
import reset.reset.Services.product.ProdutoService;
import reset.reset.dto.product.ProdutoRestauranteDTO;
import reset.reset.dto.restaurant.CategoriaRestauranteDTO;

import java.util.List;

@RestController
@RequestMapping("/restaurante/cardapio")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurante - Cardápio", description = "Gerenciamento do cardápio do restaurante")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'GARCOM')")
public class CardapioController extends BaseController {

    private final ProdutoService produtoService;

    // ========== CATEGORIAS ==========

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorias do cardápio")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<List<CategoriaRestauranteDTO>>> listarCategorias() {
        List<CategoriaRestauranteDTO> categorias = produtoService.findCategoriasRestaurante();
        return success(categorias);
    }

    // ========== ITENS ==========

    @GetMapping
    @Operation(summary = "Listar todos os itens do cardápio")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<List<ProdutoRestauranteDTO>>> listarCardapio() {
        List<ProdutoRestauranteDTO> cardapio = produtoService.findProdutosRestaurante();
        return success(cardapio);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar itens por categoria")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<List<ProdutoRestauranteDTO>>> listarPorCategoria(
            @PathVariable Long categoriaId) {
        List<ProdutoRestauranteDTO> itens = produtoService.findProdutosByCategoriaRestaurante(categoriaId);
        return success(itens);
    }

    @GetMapping("/destaques")
    @Operation(summary = "Listar itens em destaque")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<List<ProdutoRestauranteDTO>>> listarDestaques() {
        List<ProdutoRestauranteDTO> destaques = produtoService.findProdutosDestaque();
        return success(destaques);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar item do cardápio por ID")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<ProdutoRestauranteDTO>> buscarItem(@PathVariable Long id) {
        ProdutoRestauranteDTO item = produtoService.findProdutoRestauranteById(id);
        return success(item);
    }
}
