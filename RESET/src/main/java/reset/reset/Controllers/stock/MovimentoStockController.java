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
import reset.reset.dto.stock.MovimentoStockDTO;
import reset.reset.dto.stock.MovimentoStockResumoDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    public ResponseEntity<ApiResponse<MovimentoStockDTO>> findById(@PathVariable Long id) {
        MovimentoStock movimento = movimentoStockService.findByIdOrThrow(id);
        return success(MovimentoStockDTO.fromEntity(movimento));
    }

    @GetMapping
    @Operation(summary = "Get all stock movements with pagination and filtering")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<MovimentoStockDTO>>> findAll(
            @PageableDefault(size = 20) Pageable pageable,
            MovimentoStockFilter filter) {
        Page<MovimentoStockDTO> movimentos = movimentoStockService.filterDTO(filter);
        return success(movimentos);
    }

    @GetMapping("/produto/{produtoId}")
    @Operation(summary = "Get stock movements by product")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<MovimentoStockDTO>>> findByProduto(
            @PathVariable Long produtoId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovimentoStockDTO> movimentos = movimentoStockService.findByProdutoIdDTO(produtoId, pageable);
        return success(movimentos);
    }

    @GetMapping("/produto/{produtoId}/periodo")
    @Operation(summary = "Get stock movements by product and period")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<List<MovimentoStockDTO>>> findByProdutoAndPeriodo(
            @PathVariable Long produtoId,
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
        List<MovimentoStockDTO> movimentos = movimentoStockService.findMovimentosByProdutoAndPeriodoDTO(produtoId, inicio, fim);
        return success(movimentos);
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

    @GetMapping("/armazem/{armazemId}")
    @Operation(summary = "Get stock movements by warehouse")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<MovimentoStockDTO>>> findByArmazem(
            @PathVariable Long armazemId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovimentoStockDTO> movimentos = movimentoStockService.findByArmazemIdDTO(armazemId, pageable);
        return success(movimentos);
    }

    @GetMapping("/empresa")
    @Operation(summary = "Get stock movements by company")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<Page<MovimentoStockDTO>>> findByEmpresa(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovimentoStockDTO> movimentos = movimentoStockService.findByEmpresaIdDTO(pageable);
        return success(movimentos);
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Get stock movements by type")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<List<MovimentoStockResumoDTO>>> findByTipo(
            @PathVariable String tipo) {
        List<MovimentoStockResumoDTO> movimentos = movimentoStockService.findByTipoDTO(tipo);
        return success(movimentos);
    }

    @GetMapping("/referencia/{referencia}")
    @Operation(summary = "Get stock movements by reference")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<List<MovimentoStockResumoDTO>>> findByReferencia(
            @PathVariable String referencia) {
        List<MovimentoStockResumoDTO> movimentos = movimentoStockService.findByReferenciaDTO(referencia);
        return success(movimentos);
    }

    @GetMapping("/resumo/empresa")
    @Operation(summary = "Get stock movements summary by company")
    @PreAuthorize("hasPermission('STOCK_READ')")
    public ResponseEntity<ApiResponse<List<MovimentoStockResumoDTO>>> findResumoByEmpresa(
            @PageableDefault(size = 20) Pageable pageable) {
        List<MovimentoStockResumoDTO> movimentos = movimentoStockService.findResumoByEmpresaDTO(pageable);
        return success(movimentos);
    }
}