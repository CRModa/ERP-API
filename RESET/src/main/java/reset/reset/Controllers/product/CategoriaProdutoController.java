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
import reset.reset.Services.product.CategoriaProdutoService;
import reset.reset.dto.product.CategoriaProdutoResponse;
import reset.reset.dto.product.CategoriaProdutoResumoDTO;
import reset.reset.dto.request.product.CategoriaProdutoRequest;

import java.util.List;

@RestController
@RequestMapping("/categorias-produto")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categoria Produto", description = "Gerenciamento de categorias de produtos")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class CategoriaProdutoController extends BaseController {

    private final CategoriaProdutoService categoriaService;

    // ==================== CRUD ====================

    @PostMapping
    @Operation(summary = "Criar uma nova categoria")
    @PreAuthorize("hasPermission('CATEGORIA_CREATE')")
    public ResponseEntity<ApiResponse<CategoriaProdutoResponse>> criar(@Valid @RequestBody CategoriaProdutoRequest request) {
        log.info("Criando nova categoria: {}", request.getDescricao());
        CategoriaProdutoResponse response = categoriaService.criarCategoria(request);
        return created(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma categoria existente")
    @PreAuthorize("hasPermission('CATEGORIA_UPDATE')")
    public ResponseEntity<ApiResponse<CategoriaProdutoResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaProdutoRequest request) {
        log.info("Atualizando categoria com id: {}", id);
        CategoriaProdutoResponse response = categoriaService.atualizarCategoria(id, request);
        return success(response, "Categoria atualizada com sucesso");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    @PreAuthorize("hasPermission('CATEGORIA_READ')")
    public ResponseEntity<ApiResponse<CategoriaProdutoResponse>> buscarPorId(@PathVariable Long id) {
        CategoriaProdutoResponse response = categoriaService.buscarCategoriaPorId(id);
        return success(response);
    }

    @GetMapping
    @Operation(summary = "Listar categorias com paginação")
    @PreAuthorize("hasPermission('CATEGORIA_READ')")
    public ResponseEntity<ApiResponse<Page<CategoriaProdutoResumoDTO>>> listar(
            @RequestParam Long empresaId,
            @PageableDefault(size = 20, sort = "descricao", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CategoriaProdutoResumoDTO> categorias = categoriaService.listarCategoriasPorEmpresa(pageable);
        return success(categorias);
    }

    @GetMapping("/ativas")
    @Operation(summary = "Listar categorias ativas com paginação")
    @PreAuthorize("hasPermission('CATEGORIA_READ')")
    public ResponseEntity<ApiResponse<Page<CategoriaProdutoResumoDTO>>> listarAtivas(
            @RequestParam Long empresaId,
            @PageableDefault(size = 20, sort = "descricao", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CategoriaProdutoResumoDTO> categorias = categoriaService.listarCategoriasAtivasPorEmpresa(pageable);
        return success(categorias);
    }

    @GetMapping("/empresa/{empresaId}/todas")
    @Operation(summary = "Listar todas as categorias por empresa (sem paginação)")
    @PreAuthorize("hasPermission('CATEGORIA_READ')")
    public ResponseEntity<ApiResponse<List<CategoriaProdutoResponse>>> listarTodas() {
        List<CategoriaProdutoResponse> categorias = categoriaService.listarTodasCategoriasPorEmpresa();
        return success(categorias);
    }

    @GetMapping("/empresa/{empresaId}/ordem")
    @Operation(summary = "Listar categorias ordenadas por ordem")
    @PreAuthorize("hasPermission('CATEGORIA_READ')")
    public ResponseEntity<ApiResponse<List<CategoriaProdutoResponse>>> listarPorOrdem() {
        List<CategoriaProdutoResponse> categorias = categoriaService.listarCategoriasPorOrdem();
        return success(categorias);
    }

    // ==================== CANAIS ====================

    @GetMapping("/canais")
    @Operation(summary = "Listar categorias por canal de visibilidade")
    @PreAuthorize("hasPermission('CATEGORIA_READ')")
    public ResponseEntity<ApiResponse<List<CategoriaProdutoResponse>>> listarPorCanal(
            @RequestParam(required = false) String canal) {
        List<CategoriaProdutoResponse> categorias = categoriaService.listarCategoriasPorCanal(canal);
        return success(categorias);
    }

    @GetMapping("/restaurante/{empresaId}")
    @Operation(summary = "Listar categorias visíveis no restaurante")
    @PreAuthorize("hasPermission('CATEGORIA_READ')")
    public ResponseEntity<ApiResponse<List<CategoriaProdutoResponse>>> listarVisiveisRestaurante() {
        List<CategoriaProdutoResponse> categorias = categoriaService.listarCategoriasVisiveisRestaurante();
        return success(categorias);
    }

    // ==================== GESTÃO ====================

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar categoria")
    @PreAuthorize("hasPermission('CATEGORIA_UPDATE')")
    public ResponseEntity<ApiResponse<CategoriaProdutoResponse>> ativar(@PathVariable Long id) {
        CategoriaProdutoResponse response = categoriaService.ativarCategoria(id);
        return success(response, "Categoria ativada com sucesso");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar categoria")
    @PreAuthorize("hasPermission('CATEGORIA_UPDATE')")
    public ResponseEntity<ApiResponse<CategoriaProdutoResponse>> desativar(@PathVariable Long id) {
        CategoriaProdutoResponse response = categoriaService.desativarCategoria(id);
        return success(response, "Categoria desativada com sucesso");
    }

    @PatchMapping("/{id}/visibilidade")
    @Operation(summary = "Atualizar visibilidade da categoria em um canal")
    @PreAuthorize("hasPermission('CATEGORIA_UPDATE')")
    public ResponseEntity<ApiResponse<CategoriaProdutoResponse>> atualizarVisibilidadeCanal(
            @PathVariable Long id,
            @RequestParam String canal,
            @RequestParam Boolean visivel) {
        CategoriaProdutoResponse response = categoriaService.atualizarVisibilidadeCanal(id, canal, visivel);
        return success(response, "Visibilidade atualizada com sucesso");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria")
    @PreAuthorize("hasPermission('CATEGORIA_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        categoriaService.deletarCategoria(id);
        return success(null, "Categoria excluída com sucesso");
    }
}
