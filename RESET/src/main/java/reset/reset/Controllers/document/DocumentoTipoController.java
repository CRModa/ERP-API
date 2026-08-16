package reset.reset.Controllers.document;

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
import reset.reset.Models.document.DocumentoTipo;
import reset.reset.Services.document.DocumentoTipoService;
import reset.reset.dto.request.DocumentoTipoRequest;

import java.util.List;

@RestController
@RequestMapping("/documentos-tipo")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documento Tipo", description = "Document type management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class DocumentoTipoController extends BaseController {

    private final DocumentoTipoService documentoTipoService;

    @PostMapping
    @Operation(summary = "Create a new document type")
    public ResponseEntity<ApiResponse<DocumentoTipo>> create(@Valid @RequestBody DocumentoTipoRequest request) {
        log.info("Creating new documento tipo: {}", request.getDescricao());
        DocumentoTipo tipo = request.toEntity();
        DocumentoTipo saved = documentoTipoService.save(tipo);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing document type")
    public ResponseEntity<ApiResponse<DocumentoTipo>> update(@PathVariable Long id,
                                                             @Valid @RequestBody DocumentoTipoRequest request) {
        log.info("Updating documento tipo with id: {}", id);
        DocumentoTipo tipo = request.toEntity();
        tipo.setId(id);
        DocumentoTipo updated = documentoTipoService.update(id, tipo);
        return success(updated, "Documento tipo updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document type by ID")
    public ResponseEntity<ApiResponse<DocumentoTipo>> findById(@PathVariable Long id) {
        DocumentoTipo tipo = documentoTipoService.findByIdOrThrow(id);
        return success(tipo);
    }

    @GetMapping
    @Operation(summary = "Get all document types with pagination")
    public ResponseEntity<ApiResponse<Page<DocumentoTipo>>> findAll(
            @PageableDefault(size = 20, sort = "descricao", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<DocumentoTipo> tipos = documentoTipoService.findAll(pageable);
        return success(tipos);
    }

    @GetMapping("/classe/{classe}")
    @Operation(summary = "Get document types by class")
    public ResponseEntity<ApiResponse<List<DocumentoTipo>>> findByClasse(@PathVariable DocumentoTipo.ClasseDocumento classe) {
        List<DocumentoTipo> tipos = documentoTipoService.findByClasse(classe);
        return success(tipos);
    }

    @GetMapping("/movimenta-stock")
    @Operation(summary = "Get document types that affect stock")
    public ResponseEntity<ApiResponse<List<DocumentoTipo>>> findMovimentaStock() {
        List<DocumentoTipo> tipos = documentoTipoService.findTiposQueMovimentamStock();
        return success(tipos);
    }

    @GetMapping("/afeta-contas")
    @Operation(summary = "Get document types that affect accounts")
    public ResponseEntity<ApiResponse<List<DocumentoTipo>>> findAfetaContas() {
        List<DocumentoTipo> tipos = documentoTipoService.findTiposQueAfetamContas();
        return success(tipos);
    }

    @PostMapping("/inicializar")
    @Operation(summary = "Initialize default document types")
    public ResponseEntity<ApiResponse<Void>> inicializarTipos() {
        documentoTipoService.inicializarTiposDocumento();
        return success(null, "Default document types initialized");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document type")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentoTipoService.deleteById(id);
        return noContent();
    }
}
