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
import reset.reset.dto.core.EmpresaDTO;
import reset.reset.dto.core.EmpresaEstatisticasDTO;
import reset.reset.dto.core.EmpresaResumoDTO;
import reset.reset.dto.filter.EmpresaFilter;
import reset.reset.dto.request.EmpresaRequest;

import java.util.List;

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
    public ResponseEntity<ApiResponse<EmpresaDTO>> create(@Valid @RequestBody EmpresaRequest request) {
        log.info("Creating new empresa: {}", request.getNome());
        Empresa empresa = request.toEntity();
        Empresa saved = empresaService.save(empresa);
        return success(EmpresaDTO.fromEntity(saved), "Empresa created successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing company")
    public ResponseEntity<ApiResponse<EmpresaDTO>> update(@PathVariable Long id,
                                                          @Valid @RequestBody EmpresaRequest request) {
        log.info("Updating empresa with id: {}", id);
        Empresa empresa = request.toEntity();
        empresa.setId(id);
        Empresa updated = empresaService.update(id, empresa);
        return success(EmpresaDTO.fromEntity(updated), "Empresa updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID - returns full DTO")
    public ResponseEntity<ApiResponse<EmpresaDTO>> findById(@PathVariable Long id) {
        Empresa empresa = empresaService.findByIdOrThrow(id);
        return success(EmpresaDTO.fromEntity(empresa));
    }

    @GetMapping
    @Operation(summary = "Get all companies with pagination and filtering - returns summary DTOs")
    public ResponseEntity<ApiResponse<Page<EmpresaResumoDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            EmpresaFilter filter) {
        Page<EmpresaResumoDTO> empresas = empresaService.filterSummarized(filter, pageable);
        return success(empresas);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active companies - returns summary DTOs")
    public ResponseEntity<ApiResponse<Page<EmpresaResumoDTO>>> findActive(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EmpresaResumoDTO> empresas = empresaService.findActiveEmpresasSummarized(pageable);
        return success(empresas);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all companies as list - returns summary DTOs")
    public ResponseEntity<ApiResponse<List<EmpresaResumoDTO>>> findAllList() {
        List<EmpresaResumoDTO> empresas = empresaService.findAllSummarized();
        return success(empresas);
    }

    @GetMapping("/active/list")
    @Operation(summary = "Get all active companies as list - returns summary DTOs")
    public ResponseEntity<ApiResponse<List<EmpresaResumoDTO>>> findAllActiveList() {
        List<EmpresaResumoDTO> empresas = empresaService.findAllActiveSummarized();
        return success(empresas);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a company - returns full DTO")
    public ResponseEntity<ApiResponse<EmpresaDTO>> activate(@PathVariable Long id) {
        Empresa empresa = empresaService.ativarEmpresa(id);
        return success(EmpresaDTO.fromEntity(empresa), "Empresa activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a company - returns full DTO")
    public ResponseEntity<ApiResponse<EmpresaDTO>> deactivate(@PathVariable Long id) {
        Empresa empresa = empresaService.desativarEmpresa(id);
        return success(EmpresaDTO.fromEntity(empresa), "Empresa deactivated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a company")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        empresaService.deleteById(id);
        return noContent();
    }

    @GetMapping("/count")
    @Operation(summary = "Get company statistics")
    public ResponseEntity<ApiResponse<EmpresaEstatisticasDTO>> getStatistics() {
        EmpresaEstatisticasDTO stats = empresaService.getStatistics();
        return success(stats);
    }
}