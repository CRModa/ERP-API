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
import reset.reset.Models.accounting.ContaContabil;
import reset.reset.Services.accounting.ContaContabilService;
import reset.reset.dto.request.ContaContabilRequest;

import java.util.List;

@RestController
@RequestMapping("/contas-contabeis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conta Contábil", description = "Accounting account management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTABILISTA')")
public class ContaContabilController extends BaseController {

    private final ContaContabilService contaContabilService;

    @PostMapping
    @Operation(summary = "Create a new accounting account")
    @PreAuthorize("hasPermission('CONTABIL_CREATE')")
    public ResponseEntity<ApiResponse<ContaContabil>> create(@Valid @RequestBody ContaContabilRequest request) {
        log.info("Creating new conta contabil: {}", request.getCodigo());
        ContaContabil conta = request.toEntity();
        ContaContabil saved = contaContabilService.save(conta);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing accounting account")
    @PreAuthorize("hasPermission('CONTABIL_UPDATE')")
    public ResponseEntity<ApiResponse<ContaContabil>> update(@PathVariable Long id,
                                                             @Valid @RequestBody ContaContabilRequest request) {
        log.info("Updating conta contabil with id: {}", id);
        ContaContabil conta = request.toEntity();
        conta.setId(id);
        ContaContabil updated = contaContabilService.update(id, conta);
        return success(updated, "Conta contabil updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accounting account by ID")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<ContaContabil>> findById(@PathVariable Long id) {
        ContaContabil conta = contaContabilService.findByIdOrThrow(id);
        return success(conta);
    }

    @GetMapping
    @Operation(summary = "Get all accounting accounts with pagination")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<Page<ContaContabil>>> findAll(
            @PageableDefault(size = 20, sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ContaContabil> contas = contaContabilService.findAll(pageable);
        return success(contas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get accounting accounts by company")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<Page<ContaContabil>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ContaContabil> contas = contaContabilService.findByEmpresaId(empresaId, pageable);
        return success(contas);
    }

    @GetMapping("/empresa/{empresaId}/tipo/{tipo}")
    @Operation(summary = "Get accounting accounts by company and type")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<List<ContaContabil>>> findByEmpresaAndTipo(
            @PathVariable Long empresaId,
            @PathVariable String tipo) {
        List<ContaContabil> contas = contaContabilService.findByEmpresaIdAndTipo(empresaId, tipo);
        return success(contas);
    }

    @GetMapping("/empresa/{empresaId}/all")
    @Operation(summary = "Get all accounting accounts by company (no pagination)")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<List<ContaContabil>>> findAllByEmpresa(@PathVariable Long empresaId) {
        List<ContaContabil> contas = contaContabilService.findAllByEmpresaIdOrderByCodigo(empresaId);
        return success(contas);
    }

    @GetMapping("/empresa/{empresaId}/codigo/{codigo}")
    @Operation(summary = "Get accounting account by company and code")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<ContaContabil>> findByEmpresaAndCodigo(
            @PathVariable Long empresaId,
            @PathVariable String codigo) {
        ContaContabil conta = contaContabilService.findByEmpresaIdAndCodigo(empresaId, codigo);
        return success(conta);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete accounting account")
    @PreAuthorize("hasPermission('CONTABIL_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        contaContabilService.deleteById(id);
        return noContent();
    }
}
