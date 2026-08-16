package reset.reset.Controllers.customer;

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
import reset.reset.Models.customer.Fornecedor;
import reset.reset.Services.customer.FornecedorService;
import reset.reset.dto.filter.FornecedorFilter;
import reset.reset.dto.request.FornecedorRequest;

@RestController
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fornecedor", description = "Supplier management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class FornecedorController extends BaseController {

    private final FornecedorService fornecedorService;

    @PostMapping
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<ApiResponse<Fornecedor>> create(@Valid @RequestBody FornecedorRequest request) {
        log.info("Creating new fornecedor: {}", request.getNome());
        Fornecedor fornecedor = request.toEntity();
        Fornecedor saved = fornecedorService.save(fornecedor);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing supplier")
    public ResponseEntity<ApiResponse<Fornecedor>> update(@PathVariable Long id,
                                                          @Valid @RequestBody FornecedorRequest request) {
        log.info("Updating fornecedor with id: {}", id);
        Fornecedor fornecedor = request.toEntity();
        fornecedor.setId(id);
        Fornecedor updated = fornecedorService.update(id, fornecedor);
        return success(updated, "Fornecedor updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<ApiResponse<Fornecedor>> findById(@PathVariable Long id) {
        Fornecedor fornecedor = fornecedorService.findByIdOrThrow(id);
        return success(fornecedor);
    }

    @GetMapping
    @Operation(summary = "Get all suppliers with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<Fornecedor>>> findAll(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable,
            FornecedorFilter filter) {
        Page<Fornecedor> fornecedores = fornecedorService.filter(filter);
        return success(fornecedores);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get suppliers by company")
    public ResponseEntity<ApiResponse<Page<Fornecedor>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Fornecedor> fornecedores = fornecedorService.findByEmpresaId(empresaId, pageable);
        return success(fornecedores);
    }

    @GetMapping("/empresa/{empresaId}/active")
    @Operation(summary = "Get active suppliers by company")
    public ResponseEntity<ApiResponse<Page<Fornecedor>>> findActiveByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Fornecedor> fornecedores = fornecedorService.findActiveByEmpresaId(empresaId, pageable);
        return success(fornecedores);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate supplier")
    public ResponseEntity<ApiResponse<Fornecedor>> activate(@PathVariable Long id) {
        Fornecedor fornecedor = fornecedorService.ativarFornecedor(id);
        return success(fornecedor, "Fornecedor activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate supplier")
    public ResponseEntity<ApiResponse<Fornecedor>> deactivate(@PathVariable Long id) {
        Fornecedor fornecedor = fornecedorService.desativarFornecedor(id);
        return success(fornecedor, "Fornecedor deactivated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        fornecedorService.deleteById(id);
        return noContent();
    }
}
