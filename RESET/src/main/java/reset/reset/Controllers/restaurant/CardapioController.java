package reset.reset.Controllers.restaurant;

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
import reset.reset.Models.restaurant.CategoriaCardapio;
import reset.reset.Models.restaurant.ItemCardapio;
import reset.reset.Services.restaurant.CardapioService;
import reset.reset.dto.request.restaurant.CategoriaCardapioRequest;
import reset.reset.dto.request.restaurant.ItemCardapioRequest;

import java.util.List;

@RestController
@RequestMapping("/restaurante/cardapio")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurante - Cardápio", description = "Gerenciamento do cardápio do restaurante")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class CardapioController extends BaseController {

    private final CardapioService cardapioService;

    // ========== Categoria Endpoints ==========

    @PostMapping("/categorias")
    @Operation(summary = "Criar uma nova categoria")
    @PreAuthorize("hasPermission('CARDAPIO_CREATE')")
    public ResponseEntity<ApiResponse<CategoriaCardapio>> criarCategoria(@Valid @RequestBody CategoriaCardapioRequest request) {
        log.info("Criando nova categoria: {}", request.getNome());
        CategoriaCardapio categoria = request.toEntity();
        CategoriaCardapio saved = cardapioService.criarCategoria(categoria);
        return created(saved);
    }

    @PutMapping("/categorias/{id}")
    @Operation(summary = "Atualizar uma categoria existente")
    @PreAuthorize("hasPermission('CARDAPIO_UPDATE')")
    public ResponseEntity<ApiResponse<CategoriaCardapio>> atualizarCategoria(@PathVariable Long id,
                                                                             @Valid @RequestBody CategoriaCardapioRequest request) {
        log.info("Atualizando categoria com id: {}", id);
        CategoriaCardapio categoria = request.toEntity();
        categoria.setId(id);
        CategoriaCardapio updated = cardapioService.atualizarCategoria(id, categoria);
        return success(updated, "Categoria atualizada com sucesso");
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorias do cardápio")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<Page<CategoriaCardapio>>> listarCategorias(
            @RequestParam Long empresaId,
            @PageableDefault(size = 20, sort = "ordem", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CategoriaCardapio> categorias = cardapioService.findCategoriasByEmpresa(empresaId, pageable);
        return success(categorias);
    }

    @GetMapping("/categorias/ativas")
    @Operation(summary = "Listar categorias ativas do cardápio")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<List<CategoriaCardapio>>> listarCategoriasAtivas(@RequestParam Long empresaId) {
        List<CategoriaCardapio> categorias = cardapioService.findCategoriasAtivasByEmpresa(empresaId);
        return success(categorias);
    }

    @DeleteMapping("/categorias/{id}")
    @Operation(summary = "Excluir categoria")
    @PreAuthorize("hasPermission('CARDAPIO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deletarCategoria(@PathVariable Long id) {
        cardapioService.deletarCategoria(id);
        return success(null, "Categoria desativada com sucesso");
    }

    // ========== Item Cardapio Endpoints ==========

    @PostMapping("/itens")
    @Operation(summary = "Criar um novo item no cardápio")
    @PreAuthorize("hasPermission('CARDAPIO_CREATE')")
    public ResponseEntity<ApiResponse<ItemCardapio>> criarItem(@Valid @RequestBody ItemCardapioRequest request) {
        log.info("Criando novo item: {}", request.getNome());
        ItemCardapio item = request.toEntity();
        ItemCardapio saved = cardapioService.criarItem(item);
        return created(saved);
    }

    @PutMapping("/itens/{id}")
    @Operation(summary = "Atualizar um item existente")
    @PreAuthorize("hasPermission('CARDAPIO_UPDATE')")
    public ResponseEntity<ApiResponse<ItemCardapio>> atualizarItem(@PathVariable Long id,
                                                                   @Valid @RequestBody ItemCardapioRequest request) {
        log.info("Atualizando item com id: {}", id);
        ItemCardapio item = request.toEntity();
        item.setId(id);
        ItemCardapio updated = cardapioService.atualizarItem(id, item);
        return success(updated, "Item atualizado com sucesso");
    }

    @GetMapping("/itens")
    @Operation(summary = "Listar itens do cardápio")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<Page<ItemCardapio>>> listarItens(
            @RequestParam Long empresaId,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ItemCardapio> itens = cardapioService.findItensByEmpresa(empresaId, pageable);
        return success(itens);
    }

    @GetMapping("/itens/categoria/{categoriaId}")
    @Operation(summary = "Listar itens por categoria")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<Page<ItemCardapio>>> listarItensPorCategoria(
            @PathVariable Long categoriaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ItemCardapio> itens = cardapioService.findItensByCategoria(categoriaId, pageable);
        return success(itens);
    }

    @GetMapping("/itens/destaques")
    @Operation(summary = "Listar itens em destaque")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<List<ItemCardapio>>> listarDestaques(@RequestParam Long empresaId) {
        List<ItemCardapio> itens = cardapioService.findItensDestaque(empresaId);
        return success(itens);
    }

    @GetMapping("/itens/buscar")
    @Operation(summary = "Buscar itens por nome")
    @PreAuthorize("hasPermission('CARDAPIO_READ')")
    public ResponseEntity<ApiResponse<Page<ItemCardapio>>> buscarItens(
            @RequestParam Long empresaId,
            @RequestParam String nome,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ItemCardapio> itens = cardapioService.searchItens(empresaId, nome, pageable);
        return success(itens);
    }

    @PatchMapping("/itens/{id}/disponibilidade")
    @Operation(summary = "Alternar disponibilidade do item")
    @PreAuthorize("hasPermission('CARDAPIO_UPDATE')")
    public ResponseEntity<ApiResponse<ItemCardapio>> toggleDisponibilidade(@PathVariable Long id) {
        ItemCardapio item = cardapioService.toggleDisponibilidade(id);
        return success(item, "Disponibilidade alterada com sucesso");
    }
}
