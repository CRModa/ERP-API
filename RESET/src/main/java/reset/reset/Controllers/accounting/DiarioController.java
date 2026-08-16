package reset.reset.Controllers.accounting;

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
import reset.reset.Models.accounting.Diario;
import reset.reset.Services.accounting.DiarioService;
import reset.reset.dto.request.DiarioRequest;

import java.util.List;

@RestController
@RequestMapping("/diarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Diário", description = "Journal management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTABILISTA')")
public class DiarioController extends BaseController {

    private final DiarioService diarioService;

    @PostMapping
    @Operation(summary = "Create a new journal")
    @PreAuthorize("hasPermission('CONTABIL_CREATE')")
    public ResponseEntity<ApiResponse<Diario>> create(@Valid @RequestBody DiarioRequest request) {
        log.info("Creating new diario: {}", request.getCodigo());
        Diario diario = request.toEntity();
        Diario saved = diarioService.save(diario);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing journal")
    @PreAuthorize("hasPermission('CONTABIL_UPDATE')")
    public ResponseEntity<ApiResponse<Diario>> update(@PathVariable Long id,
                                                      @Valid @RequestBody DiarioRequest request) {
        log.info("Updating diario with id: {}", id);
        Diario diario = request.toEntity();
        diario.setId(id);
        Diario updated = diarioService.update(id, diario);
        return success(updated, "Diario updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get journal by ID")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<Diario>> findById(@PathVariable Long id) {
        Diario diario = diarioService.findByIdOrThrow(id);
        return success(diario);
    }

    @GetMapping
    @Operation(summary = "Get all journals with pagination")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<Page<Diario>>> findAll(
            @PageableDefault(size = 20, sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Diario> diarios = diarioService.findAll(pageable);
        return success(diarios);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get journals by company")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<Page<Diario>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Diario> diarios = diarioService.findByEmpresaId(empresaId, pageable);
        return success(diarios);
    }

    @GetMapping("/empresa/{empresaId}/all")
    @Operation(summary = "Get all journals by company (no pagination)")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<List<Diario>>> findAllByEmpresa(@PathVariable Long empresaId) {
        List<Diario> diarios = diarioService.findAllByEmpresaIdOrderByCodigo(empresaId);
        return success(diarios);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete journal")
    @PreAuthorize("hasPermission('CONTABIL_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        diarioService.deleteById(id);
        return noContent();
    }
}
