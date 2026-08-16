package reset.reset.Controllers.stock;

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
import reset.reset.Models.stock.Armazem;
import reset.reset.Services.stock.ArmazemService;
import reset.reset.dto.request.ArmazemRequest;

import java.util.List;

@RestController
@RequestMapping("/armazens")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Armazem", description = "Warehouse management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class ArmazemController extends BaseController {

    private final ArmazemService armazemService;

    @PostMapping
    @Operation(summary = "Create a new warehouse")
    public ResponseEntity<ApiResponse<Armazem>> create(@Valid @RequestBody ArmazemRequest request) {
        log.info("Creating new armazem: {}", request.getNome());
        Armazem armazem = request.toEntity();
        Armazem saved = armazemService.save(armazem);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing warehouse")
    public ResponseEntity<ApiResponse<Armazem>> update(@PathVariable Long id,
                                                       @Valid @RequestBody ArmazemRequest request) {
        log.info("Updating armazem with id: {}", id);
        Armazem armazem = request.toEntity();
        armazem.setId(id);
        Armazem updated = armazemService.update(id, armazem);
        return success(updated, "Armazem updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID")
    public ResponseEntity<ApiResponse<Armazem>> findById(@PathVariable Long id) {
        Armazem armazem = armazemService.findByIdOrThrow(id);
        return success(armazem);
    }

    @GetMapping
    @Operation(summary = "Get all warehouses")
    public ResponseEntity<ApiResponse<Page<Armazem>>> findAll(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Armazem> armazens = armazemService.findAll(pageable);
        return success(armazens);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get warehouses by company")
    public ResponseEntity<ApiResponse<Page<Armazem>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Armazem> armazens = armazemService.findByEmpresaId(empresaId, pageable);
        return success(armazens);
    }

    @GetMapping("/empresa/{empresaId}/all")
    @Operation(summary = "Get all warehouses by company (no pagination)")
    public ResponseEntity<ApiResponse<List<Armazem>>> findAllByEmpresa(@PathVariable Long empresaId) {
        List<Armazem> armazens = armazemService.findAllByEmpresaIdOrderByNome(empresaId);
        return success(armazens);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete warehouse")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        armazemService.deleteById(id);
        return noContent();
    }
}