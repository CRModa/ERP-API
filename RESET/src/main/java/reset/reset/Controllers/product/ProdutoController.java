package reset.reset.Controllers.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Controllers.base.BaseController;
import reset.reset.Models.product.Produto;
import reset.reset.Services.product.ProdutoService;
import reset.reset.dto.filter.ProdutoFilter;
import reset.reset.dto.product.ProdutoCompostoDTO;
import reset.reset.dto.product.ProdutoDTO;
import reset.reset.dto.product.ProdutoResumoDTO;
import reset.reset.dto.product.ProdutoRestauranteDTO;
import reset.reset.dto.request.product.ProdutoCompostoRequest;
import reset.reset.dto.request.product.ProdutoRequest;
import reset.reset.dto.restaurant.CategoriaRestauranteDTO;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Produto", description = "Product management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE', 'CONTABILISTA')")
public class ProdutoController extends BaseController {

    private final ProdutoService produtoService;

    // ==================== CRUD PRODUTO SIMPLES ====================

    @PostMapping
    @Operation(summary = "Create a new simple product")
    @PreAuthorize("hasPermission('PRODUTO_CREATE')")
    public ResponseEntity<ApiResponse<ProdutoDTO>> create(@Valid @RequestBody ProdutoRequest request) {
        log.info("Creating new produto: {}", request.getNome());
        Produto produto = produtoService.criarProdutoSimples(request);
        return created(ProdutoDTO.fromEntity(produto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a simple product")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<ProdutoDTO>> update(@PathVariable Long id,
                                                          @Valid @RequestBody ProdutoRequest request) {
        log.info("Updating produto with id: {}", id);
        Produto updated = produtoService.atualizarProdutoSimples(id, request);
        return success(ProdutoDTO.fromEntity(updated), "Produto updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<ProdutoDTO>> findById(@PathVariable Long id) {
        Produto produto = produtoService.findByIdOrThrow(id);
        return success(ProdutoDTO.fromEntity(produto));
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination and filtering")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<ProdutoDTO>>> findAll(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable,
            ProdutoFilter filter) {
        Page<ProdutoDTO> produtos = produtoService.filterDTO(filter);
        return success(produtos);
    }

    @GetMapping("/empresa")
    @Operation(summary = "Get products by company")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<ProdutoDTO>>> findByEmpresa(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProdutoDTO> produtos = produtoService.findByEmpresaIdDTO(pageable);
        return success(produtos);
    }

    @GetMapping("/empresa/active")
    @Operation(summary = "Get active products by company")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<ProdutoDTO>>> findActiveByEmpresa(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProdutoDTO> produtos = produtoService.findActiveByEmpresaIdDTO(pageable);
        return success(produtos);
    }

    @GetMapping("/empresa/resumo")
    @Operation(summary = "Get product summary by company")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<ProdutoResumoDTO>>> findResumoByEmpresa(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProdutoResumoDTO> produtos = produtoService.findProdutoResumoByEmpresaIdDTO(pageable);
        return success(produtos);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Get products by category")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<ProdutoDTO>>> findByCategoria(
            @PathVariable Long categoriaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProdutoDTO> produtos = produtoService.findByCategoriaIdDTO(categoriaId, pageable);
        return success(produtos);
    }

    // ==================== CRUD PRODUTO COMPOSTO ====================

    @PostMapping("/composto")
    @Operation(summary = "Create a new composed product")
    @PreAuthorize("hasPermission('PRODUTO_CREATE')")
    public ResponseEntity<ApiResponse<ProdutoCompostoDTO>> createComposto(
            @Valid @RequestBody ProdutoCompostoRequest request) {
        log.info("Creating new produto composto: {}", request.getNome());
        ProdutoCompostoDTO dto = produtoService.criarProdutoComposto(request);
        return created(dto);
    }

    @PutMapping("/composto/{id}")
    @Operation(summary = "Update a composed product")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<ProdutoCompostoDTO>> updateComposto(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoCompostoRequest request) {
        log.info("Updating produto composto with id: {}", id);
        ProdutoCompostoDTO dto = produtoService.atualizarProdutoComposto(id, request);
        return success(dto, "Produto composto updated successfully");
    }

    @GetMapping("/composto/{id}")
    @Operation(summary = "Get composed product by ID")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<ProdutoCompostoDTO>> findCompostoById(@PathVariable Long id) {
        ProdutoCompostoDTO dto = produtoService.findCompostoByIdDTO(id);
        return success(dto);
    }

    @GetMapping("/composto/empresa")
    @Operation(summary = "Get all composed products by company")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<List<ProdutoCompostoDTO>>> findAllCompostosByEmpresa() {
        List<ProdutoCompostoDTO> produtos = produtoService.findCompostosByEmpresaDTO();
        return success(produtos);
    }

    // ==================== ENDPOINTS PARA RESTAURANTE ====================

    @GetMapping("/restaurante")
    @Operation(summary = "Get all products for restaurant display")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<ProdutoRestauranteDTO>>> findProdutosRestaurante() {
        List<ProdutoRestauranteDTO> produtos = produtoService.findProdutosRestaurante();
        return success(produtos);
    }

    @GetMapping("/restaurante/categoria/{categoriaId}")
    @Operation(summary = "Get products by category for restaurant display")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<ProdutoRestauranteDTO>>> findProdutosByCategoriaRestaurante(
            @PathVariable Long categoriaId) {
        List<ProdutoRestauranteDTO> produtos = produtoService.findProdutosByCategoriaRestaurante(categoriaId);
        return success(produtos);
    }

    @GetMapping("/restaurante/destaque")
    @Operation(summary = "Get featured products for restaurant display")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<ProdutoRestauranteDTO>>> findProdutosDestaque() {
        List<ProdutoRestauranteDTO> produtos = produtoService.findProdutosDestaque();
        return success(produtos);
    }

    @GetMapping("/restaurante/{id}")
    @Operation(summary = "Get product by ID for restaurant display")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<ProdutoRestauranteDTO>> findProdutoRestauranteById(@PathVariable Long id) {
        ProdutoRestauranteDTO produto = produtoService.findProdutoRestauranteById(id);
        return success(produto);
    }

    @GetMapping("/restaurante/categorias")
    @Operation(summary = "Get all categories with products for restaurant display")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<CategoriaRestauranteDTO>>> findCategoriasRestaurante() {
        List<CategoriaRestauranteDTO> categorias = produtoService.findCategoriasRestaurante();
        return success(categorias);
    }

    // ==================== MÉTODOS DE GESTÃO ====================

    @PatchMapping("/{id}/preco-venda")
    @Operation(summary = "Update product selling price")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<ProdutoDTO>> updatePrecoVenda(@PathVariable Long id,
                                                                    @RequestParam BigDecimal preco) {
        Produto produto = produtoService.atualizarPrecoVenda(id, preco);
        return success(ProdutoDTO.fromEntity(produto), "Preço de venda updated successfully");
    }

    @PatchMapping("/{id}/preco-custo")
    @Operation(summary = "Update product cost price")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<ProdutoDTO>> updatePrecoCusto(@PathVariable Long id,
                                                                    @RequestParam BigDecimal preco) {
        Produto produto = produtoService.atualizarPrecoCusto(id, preco);
        return success(ProdutoDTO.fromEntity(produto), "Preço de custo updated successfully");
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate product")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<ProdutoDTO>> activate(@PathVariable Long id) {
        Produto produto = produtoService.ativarProduto(id);
        return success(ProdutoDTO.fromEntity(produto), "Produto activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate product")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<ProdutoDTO>> deactivate(@PathVariable Long id) {
        Produto produto = produtoService.desativarProduto(id);
        return success(ProdutoDTO.fromEntity(produto), "Produto deactivated successfully");
    }

    @PatchMapping("/{id}/toggle-disponibilidade")
    @Operation(summary = "Toggle product availability")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<ProdutoDTO>> toggleDisponibilidade(@PathVariable Long id) {
        Produto produto = produtoService.toggleDisponibilidade(id);
        return success(ProdutoDTO.fromEntity(produto), "Disponibilidade toggled successfully");
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<List<ProdutoDTO>>> findByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<ProdutoDTO> produtos = produtoService.findProdutosByPriceRangeDTO(minPrice, maxPrice);
        return success(produtos);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    @PreAuthorize("hasPermission('PRODUTO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        produtoService.deleteById(id);
        return noContent();
    }
}
