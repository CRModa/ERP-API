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
import reset.reset.dto.projection.ProdutoResumo;
import reset.reset.dto.request.product.ProdutoRequest;

import java.math.BigDecimal;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Produto", description = "Product management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE', 'CONTABILISTA')")
public class ProdutoController extends BaseController {

    private final ProdutoService produtoService;

    @PostMapping
    @Operation(summary = "Create a new product")
    @PreAuthorize("hasPermission('PRODUTO_CREATE')")
    public ResponseEntity<ApiResponse<Produto>> create(@Valid @RequestBody ProdutoRequest request) {
        log.info("Creating new produto: {}", request.getNome());
        Produto produto = request.toEntity();
        Produto saved = produtoService.save(produto);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<Produto>> update(@PathVariable Long id,
                                                       @Valid @RequestBody ProdutoRequest request) {
        log.info("Updating produto with id: {}", id);
        Produto produto = request.toEntity();
        produto.setId(id);
        Produto updated = produtoService.update(id, produto);
        return success(updated, "Produto updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Produto>> findById(@PathVariable Long id) {
        Produto produto = produtoService.findByIdOrThrow(id);
        return success(produto);
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination and filtering")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<Produto>>> findAll(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable,
            ProdutoFilter filter) {
        Page<Produto> produtos = produtoService.filter(filter);
        return success(produtos);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get products by company")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<Produto>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Produto> produtos = produtoService.findByEmpresaId(empresaId, pageable);
        return success(produtos);
    }

    @GetMapping("/empresa/{empresaId}/active")
    @Operation(summary = "Get active products by company")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<Produto>>> findActiveByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Produto> produtos = produtoService.findActiveByEmpresaId(empresaId, pageable);
        return success(produtos);
    }

    @GetMapping("/empresa/{empresaId}/resumo")
    @Operation(summary = "Get product summary by company")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<ProdutoResumo>>> findResumoByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProdutoResumo> produtos = produtoService.findProdutoResumoByEmpresaId(empresaId, pageable);
        return success(produtos);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Get products by category")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<Produto>>> findByCategoria(
            @PathVariable Long categoriaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Produto> produtos = produtoService.findByCategoriaId(categoriaId, pageable);
        return success(produtos);
    }

    @PatchMapping("/{id}/preco-venda")
    @Operation(summary = "Update product selling price")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<Produto>> updatePrecoVenda(@PathVariable Long id,
                                                                 @RequestParam BigDecimal preco) {
        Produto produto = produtoService.atualizarPrecoVenda(id, preco);
        return success(produto, "Preço de venda updated successfully");
    }

    @PatchMapping("/{id}/preco-custo")
    @Operation(summary = "Update product cost price")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<Produto>> updatePrecoCusto(@PathVariable Long id,
                                                                 @RequestParam BigDecimal preco) {
        Produto produto = produtoService.atualizarPrecoCusto(id, preco);
        return success(produto, "Preço de custo updated successfully");
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate product")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<Produto>> activate(@PathVariable Long id) {
        Produto produto = produtoService.ativarProduto(id);
        return success(produto, "Produto activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate product")
    @PreAuthorize("hasPermission('PRODUTO_UPDATE')")
    public ResponseEntity<ApiResponse<Produto>> deactivate(@PathVariable Long id) {
        Produto produto = produtoService.desativarProduto(id);
        return success(produto, "Produto deactivated successfully");
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range")
    @PreAuthorize("hasPermission('PRODUTO_READ')")
    public ResponseEntity<ApiResponse<Page<Produto>>> findByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        // Add method to service
        return success(Page.empty());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    @PreAuthorize("hasPermission('PRODUTO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        produtoService.deleteById(id);
        return noContent();
    }
}
