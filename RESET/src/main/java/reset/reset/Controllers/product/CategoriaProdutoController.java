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
import reset.reset.Models.product.CategoriaProduto;
import reset.reset.Services.product.CategoriaProdutoService;
import reset.reset.dto.request.CategoriaProdutoRequest;

import java.util.List;

@RestController
@RequestMapping("/categorias-produto")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categoria Produto", description = "Product category management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class CategoriaProdutoController extends BaseController {

    private final CategoriaProdutoService categoriaService;

    @PostMapping
    @Operation(summary = "Create a new product category")
    public ResponseEntity<ApiResponse<CategoriaProduto>> create(@Valid @RequestBody CategoriaProdutoRequest request) {
        log.info("Creating new categoria: {}", request.getDescricao());
        CategoriaProduto categoria = request.toEntity();
        CategoriaProduto saved = categoriaService.save(categoria);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product category")
    public ResponseEntity<ApiResponse<CategoriaProduto>> update(@PathVariable Long id,
                                                                @Valid @RequestBody CategoriaProdutoRequest request) {
        log.info("Updating categoria with id: {}", id);
        CategoriaProduto categoria = request.toEntity();
        categoria.setId(id);
        CategoriaProduto updated = categoriaService.update(id, categoria);
        return success(updated, "Categoria updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoriaProduto>> findById(@PathVariable Long id) {
        CategoriaProduto categoria = categoriaService.findByIdOrThrow(id);
        return success(categoria);
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<ApiResponse<Page<CategoriaProduto>>> findAll(
            @PageableDefault(size = 20, sort = "descricao", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CategoriaProduto> categorias = categoriaService.findAll(pageable);
        return success(categorias);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get categories by company")
    public ResponseEntity<ApiResponse<Page<CategoriaProduto>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CategoriaProduto> categorias = categoriaService.findByEmpresaId(empresaId, pageable);
        return success(categorias);
    }

    @GetMapping("/empresa/{empresaId}/all")
    @Operation(summary = "Get all categories by company (no pagination)")
    public ResponseEntity<ApiResponse<List<CategoriaProduto>>> findAllByEmpresa(@PathVariable Long empresaId) {
        List<CategoriaProduto> categorias = categoriaService.findAllByEmpresaIdOrderByDescricao(empresaId);
        return success(categorias);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoriaService.deleteById(id);
        return noContent();
    }
}
