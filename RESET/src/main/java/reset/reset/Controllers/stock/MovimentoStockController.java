package reset.reset.Controllers.stock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Controllers.base.BaseController;
import reset.reset.Models.stock.MovimentoStock;
import reset.reset.Services.stock.MovimentoStockService;
import reset.reset.dto.filter.MovimentoStockFilter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/movimentos-stock")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movimento Stock", description = "Stock movement management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class MovimentoStockController extends BaseController {

    private final MovimentoStockService movimentoStockService;

    @GetMapping("/{id}")
    @Operation(summary = "Get stock movement by ID")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<MovimentoStock>> findById(@PathVariable Long id) {
        MovimentoStock movimento = movimentoStockService.findByIdOrThrow(id);
        return success(movimento);
    }

    @GetMapping
    @Operation(summary = "Get all stock movements with pagination and filtering")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<MovimentoStock>>> findAll(
            @PageableDefault(size = 20) Pageable pageable,
            MovimentoStockFilter filter) {
        Page<MovimentoStock> movimentos = movimentoStockService.filter(filter);
        return success(movimentos);
    }

    @GetMapping("/produto/{produtoId}")
    @Operation(summary = "Get stock movements by product")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<MovimentoStock>>> findByProduto(
            @PathVariable Long produtoId,
            @PageableDefault(size = 20) Pageable pageable) {
        // Add method to service
        return success(Page.empty());
    }

    @GetMapping("/produto/{produtoId}/periodo")
    @Operation(summary = "Get stock movements by product and period")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<MovimentoStock>>> findByProdutoAndPeriodo(
            @PathVariable Long produtoId,
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim,
            @PageableDefault(size = 20) Pageable pageable) {
        // Add method to service
        return success(Page.empty());
    }

    @GetMapping("/produto/{produtoId}/sum/{tipo}")
    @Operation(summary = "Sum quantities by product and movement type")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> sumQuantidadeByProdutoAndTipo(
            @PathVariable Long produtoId,
            @PathVariable String tipo) {
        BigDecimal sum = movimentoStockService.sumQuantidadeByProdutoAndTipo(produtoId, tipo);
        return success(sum);
    }
}
