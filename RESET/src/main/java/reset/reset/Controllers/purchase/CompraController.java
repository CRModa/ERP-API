package reset.reset.Controllers.purchase;

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
import reset.reset.Models.purchase.Compra;
import reset.reset.Services.purchase.CompraService;
import reset.reset.dto.filter.BaseFilter;
import reset.reset.dto.request.CompraRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Compra", description = "Purchase management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class CompraController extends BaseController {

    private final CompraService compraService;

    @PostMapping
    @Operation(summary = "Create a new purchase")
    @PreAuthorize("hasPermission('COMPRA_CREATE')")
    public ResponseEntity<ApiResponse<Compra>> create(@Valid @RequestBody CompraRequest request) {
        log.info("Creating new compra");
        Compra compra = request.toEntity();
        Compra saved = compraService.save(compra);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing purchase")
    @PreAuthorize("hasPermission('COMPRA_UPDATE')")
    public ResponseEntity<ApiResponse<Compra>> update(@PathVariable Long id,
                                                      @Valid @RequestBody CompraRequest request) {
        log.info("Updating compra with id: {}", id);
        Compra compra = request.toEntity();
        compra.setId(id);
        Compra updated = compraService.update(id, compra);
        return success(updated, "Compra updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase by ID")
    @PreAuthorize("hasPermission('COMPRA_READ')")
    public ResponseEntity<ApiResponse<Compra>> findById(@PathVariable Long id) {
        Compra compra = compraService.findByIdOrThrow(id);
        return success(compra);
    }

    @GetMapping
    @Operation(summary = "Get all purchases with pagination and filtering")
    @PreAuthorize("hasPermission('COMPRA_READ')")
    public ResponseEntity<ApiResponse<Page<Compra>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            BaseFilter filter) {
        Page<Compra> compras = compraService.filter(filter);
        return success(compras);
    }

    @GetMapping("/fornecedor/{fornecedorId}")
    @Operation(summary = "Get purchases by supplier")
    @PreAuthorize("hasPermission('COMPRA_READ')")
    public ResponseEntity<ApiResponse<Page<Compra>>> findByFornecedor(
            @PathVariable Long fornecedorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Compra> compras = compraService.findByFornecedorId(fornecedorId, pageable);
        return success(compras);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Get purchases by status")
    @PreAuthorize("hasPermission('COMPRA_READ')")
    public ResponseEntity<ApiResponse<Page<Compra>>> findByEstado(
            @PathVariable String estado,
            @PageableDefault(size = 20) Pageable pageable) {
        // Add method to service
        return success(Page.empty());
    }

    @GetMapping("/empresa/{empresaId}/total-periodo")
    @Operation(summary = "Get total purchase value by company and period")
    @PreAuthorize("hasPermission('COMPRA_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalByEmpresaAndPeriodo(
            @PathVariable Long empresaId,
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim) {
        BigDecimal total = compraService.sumTotalByEmpresaAndPeriodo(empresaId, inicio, fim);
        return success(total);
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirm purchase")
    @PreAuthorize("hasPermission('COMPRA_UPDATE')")
    public ResponseEntity<ApiResponse<Compra>> confirmar(@PathVariable Long id) {
        Compra compra = compraService.confirmarCompra(id);
        return success(compra, "Compra confirmed");
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Finalize purchase (updates stock)")
    @PreAuthorize("hasPermission('COMPRA_UPDATE')")
    public ResponseEntity<ApiResponse<Compra>> finalizar(@PathVariable Long id,
                                                         @RequestParam Long armazemId) {
        Compra compra = compraService.finalizarCompra(id, armazemId);
        return success(compra, "Compra finalized and stock updated");
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancel purchase")
    @PreAuthorize("hasPermission('COMPRA_UPDATE')")
    public ResponseEntity<ApiResponse<Compra>> cancelar(@PathVariable Long id,
                                                        @RequestParam String motivo) {
        Compra compra = compraService.cancelarCompra(id, motivo);
        return success(compra, "Compra cancelled");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete purchase")
    @PreAuthorize("hasPermission('COMPRA_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        compraService.deleteById(id);
        return noContent();
    }
}
