package reset.reset.Controllers.core;

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
import reset.reset.Models.core.Empresa;
import reset.reset.Services.core.EmpresaService;
import reset.reset.dto.filter.EmpresaFilter;
import reset.reset.dto.request.EmpresaRequest;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Empresa", description = "Company management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class EmpresaController extends BaseController {

    private final EmpresaService empresaService;

    @PostMapping
    @Operation(summary = "Create a new company")
    public ResponseEntity<ApiResponse<Empresa>> create(@Valid @RequestBody EmpresaRequest request) {
        log.info("Creating new empresa: {}", request.getNome());
        Empresa empresa = request.toEntity();
        Empresa saved = empresaService.save(empresa);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing company")
    public ResponseEntity<ApiResponse<Empresa>> update(@PathVariable Long id,
                                                       @Valid @RequestBody EmpresaRequest request) {
        log.info("Updating empresa with id: {}", id);
        Empresa empresa = request.toEntity();
        empresa.setId(id);
        Empresa updated = empresaService.update(id, empresa);
        return success(updated, "Empresa updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID")
    public ResponseEntity<ApiResponse<Empresa>> findById(@PathVariable Long id) {
        Empresa empresa = empresaService.findByIdOrThrow(id);
        return success(empresa);
    }

    @GetMapping
    @Operation(summary = "Get all companies with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<Empresa>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            EmpresaFilter filter) {
        Page<Empresa> empresas = empresaService.filter(filter);
        return success(empresas);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active companies")
    public ResponseEntity<ApiResponse<Page<Empresa>>> findActive(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Empresa> empresas = empresaService.findActiveEmpresas(pageable);
        return success(empresas);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a company")
    public ResponseEntity<ApiResponse<Empresa>> activate(@PathVariable Long id) {
        Empresa empresa = empresaService.ativarEmpresa(id);
        return success(empresa, "Empresa activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a company")
    public ResponseEntity<ApiResponse<Empresa>> deactivate(@PathVariable Long id) {
        Empresa empresa = empresaService.desativarEmpresa(id);
        return success(empresa, "Empresa deactivated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a company")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        empresaService.deleteById(id);
        return noContent();
    }
}