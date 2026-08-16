package reset.reset.Controllers.stock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import reset.reset.Models.stock.Stock;
import reset.reset.Services.stock.StockService;
import reset.reset.dto.filter.StockFilter;
import reset.reset.dto.projection.StockResumo;
import reset.reset.dto.request.AjusteStockRequest;
import reset.reset.dto.request.TransferenciaStockRequest;

import java.math.BigDecimal;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stock", description = "Stock management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class StockController extends BaseController {

    private final StockService stockService;

    @GetMapping("/produto/{produtoId}/armazem/{armazemId}")
    @Operation(summary = "Get stock by product and warehouse")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Stock>> getStockByProdutoAndArmazem(
            @PathVariable Long produtoId,
            @PathVariable Long armazemId) {
        Stock stock = stockService.getStockByProdutoAndArmazem(produtoId, armazemId);
        return success(stock);
    }

    @GetMapping("/produto/{produtoId}")
    @Operation(summary = "Get total stock quantity for a product across all warehouses")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getQuantidadeTotalPorProduto(@PathVariable Long produtoId) {
        BigDecimal total = stockService.getQuantidadeTotalPorProduto(produtoId);
        return success(total);
    }

    @GetMapping
    @Operation(summary = "Get all stock with pagination and filtering")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<Stock>>> findAll(
            @PageableDefault(size = 20) Pageable pageable,
            StockFilter filter) {
        Page<Stock> stocks = stockService.filter(filter);
        return success(stocks);
    }

    @GetMapping("/empresa/{empresaId}/resumo")
    @Operation(summary = "Get stock summary by company")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<StockResumo>>> findResumoByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<StockResumo> stocks = stockService.findStockResumoByEmpresaId(empresaId, pageable);
        return success(stocks);
    }

    @GetMapping("/armazem/{armazemId}")
    @Operation(summary = "Get stock by warehouse")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<Stock>>> findByArmazem(
            @PathVariable Long armazemId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Stock> stocks = stockService.findByArmazemId(armazemId, pageable);
        return success(stocks);
    }

    @PostMapping("/adicionar")
    @Operation(summary = "Add stock to a product in a warehouse")
    @PreAuthorize("hasPermission('STOCK_UPDATE')")
    public ResponseEntity<ApiResponse<Stock>> adicionarStock(
            @RequestParam Long produtoId,
            @RequestParam Long armazemId,
            @RequestParam BigDecimal quantidade,
            @RequestParam String referencia) {
        Stock stock = stockService.adicionarStock(produtoId, armazemId, quantidade, referencia);
        return success(stock, "Stock added successfully");
    }

    @PostMapping("/remover")
    @Operation(summary = "Remove stock from a product in a warehouse")
    @PreAuthorize("hasPermission('STOCK_UPDATE')")
    public ResponseEntity<ApiResponse<Stock>> removerStock(
            @RequestParam Long produtoId,
            @RequestParam Long armazemId,
            @RequestParam BigDecimal quantidade,
            @RequestParam String referencia) {
        Stock stock = stockService.removerStock(produtoId, armazemId, quantidade, referencia);
        return success(stock, "Stock removed successfully");
    }

    @PatchMapping("/ajustar")
    @Operation(summary = "Adjust stock quantity")
    @PreAuthorize("hasPermission('STOCK_UPDATE')")
    public ResponseEntity<ApiResponse<Stock>> ajustarStock(@Valid @RequestBody AjusteStockRequest request) {
        Stock stock = stockService.ajustarStock(
                request.getProdutoId(),
                request.getArmazemId(),
                request.getNovaQuantidade(),
                request.getMotivo()
        );
        return success(stock, "Stock adjusted successfully");
    }

    @PostMapping("/transferir")
    @Operation(summary = "Transfer stock between warehouses")
    @PreAuthorize("hasPermission('STOCK_UPDATE')")
    public ResponseEntity<ApiResponse<Stock>> transferirStock(@Valid @RequestBody TransferenciaStockRequest request) {
        Stock stock = stockService.transferirStock(
                request.getProdutoId(),
                request.getOrigemArmazemId(),
                request.getDestinoArmazemId(),
                request.getQuantidade(),
                request.getReferencia()
        );
        return success(stock, "Stock transferred successfully");
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get products with low stock")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<Stock>>> findLowStock(
            @RequestParam(defaultValue = "10") BigDecimal threshold,
            @PageableDefault(size = 20) Pageable pageable) {
        // Add method to service
        return success(Page.empty());
    }
}
