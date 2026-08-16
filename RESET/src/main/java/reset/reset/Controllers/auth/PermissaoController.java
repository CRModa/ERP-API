package reset.reset.Controllers.auth;

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
import reset.reset.Models.auth.Permissao;
import reset.reset.Services.auth.PermissaoService;
import reset.reset.dto.request.PermissaoRequest;

import java.util.List;

@RestController
@RequestMapping("/permissoes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permissao", description = "Permission management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class PermissaoController extends BaseController {

    private final PermissaoService permissaoService;

    @PostMapping
    @Operation(summary = "Create a new permission")
    public ResponseEntity<ApiResponse<Permissao>> create(@Valid @RequestBody PermissaoRequest request) {
        log.info("Creating new permission: {}", request.getNome());
        Permissao permissao = request.toEntity();
        Permissao saved = permissaoService.save(permissao);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing permission")
    public ResponseEntity<ApiResponse<Permissao>> update(@PathVariable Long id,
                                                         @Valid @RequestBody PermissaoRequest request) {
        log.info("Updating permission with id: {}", id);
        Permissao permissao = request.toEntity();
        permissao.setId(id);
        Permissao updated = permissaoService.update(id, permissao);
        return success(updated, "Permission updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<ApiResponse<Permissao>> findById(@PathVariable Long id) {
        Permissao permissao = permissaoService.findByIdOrThrow(id);
        return success(permissao);
    }

    @GetMapping
    @Operation(summary = "Get all permissions")
    public ResponseEntity<ApiResponse<Page<Permissao>>> findAll(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Permissao> permissoes = permissaoService.findAll(pageable);
        return success(permissoes);
    }

    @GetMapping("/recurso/{recurso}")
    @Operation(summary = "Get permissions by resource")
    public ResponseEntity<ApiResponse<List<Permissao>>> findByRecurso(@PathVariable String recurso) {
        List<Permissao> permissoes = permissaoService.findByRecurso(recurso);
        return success(permissoes);
    }

    @GetMapping("/acao/{acao}")
    @Operation(summary = "Get permissions by action")
    public ResponseEntity<ApiResponse<List<Permissao>>> findByAcao(@PathVariable String acao) {
        List<Permissao> permissoes = permissaoService.findByAcao(acao);
        return success(permissoes);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        permissaoService.deleteById(id);
        return noContent();
    }
}
