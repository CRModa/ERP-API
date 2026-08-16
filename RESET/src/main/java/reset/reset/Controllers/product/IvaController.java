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
import reset.reset.Models.product.Iva;
import reset.reset.Services.product.IvaService;
import reset.reset.dto.request.IvaRequest;

import java.util.List;

@RestController
@RequestMapping("/iva")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "IVA", description = "Tax management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTABILISTA', 'GERENTE')")
public class IvaController extends BaseController {

    private final IvaService ivaService;

    @PostMapping
    @Operation(summary = "Create a new tax")
    public ResponseEntity<ApiResponse<Iva>> create(@Valid @RequestBody IvaRequest request) {
        log.info("Creating new IVA: {}", request.getCodigo());
        Iva iva = request.toEntity();
        Iva saved = ivaService.save(iva);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing tax")
    public ResponseEntity<ApiResponse<Iva>> update(@PathVariable Long id,
                                                   @Valid @RequestBody IvaRequest request) {
        log.info("Updating IVA with id: {}", id);
        Iva iva = request.toEntity();
        iva.setId(id);
        Iva updated = ivaService.update(id, iva);
        return success(updated, "IVA updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tax by ID")
    public ResponseEntity<ApiResponse<Iva>> findById(@PathVariable Long id) {
        Iva iva = ivaService.findByIdOrThrow(id);
        return success(iva);
    }

    @GetMapping
    @Operation(summary = "Get all taxes")
    public ResponseEntity<ApiResponse<Page<Iva>>> findAll(
            @PageableDefault(size = 20, sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Iva> ivas = ivaService.findAll(pageable);
        return success(ivas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get taxes by company")
    public ResponseEntity<ApiResponse<Page<Iva>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Iva> ivas = ivaService.findByEmpresaId(empresaId, pageable);
        return success(ivas);
    }

    @GetMapping("/empresa/{empresaId}/active")
    @Operation(summary = "Get active taxes by company")
    public ResponseEntity<ApiResponse<List<Iva>>> findActiveByEmpresa(@PathVariable Long empresaId) {
        List<Iva> ivas = ivaService.findActiveByEmpresaId(empresaId);
        return success(ivas);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate tax")
    public ResponseEntity<ApiResponse<Iva>> activate(@PathVariable Long id) {
        Iva iva = ivaService.ativarIva(id);
        return success(iva, "IVA activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate tax")
    public ResponseEntity<ApiResponse<Iva>> deactivate(@PathVariable Long id) {
        Iva iva = ivaService.desativarIva(id);
        return success(iva, "IVA deactivated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tax")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ivaService.deleteById(id);
        return noContent();
    }
}