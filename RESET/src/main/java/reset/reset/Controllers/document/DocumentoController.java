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
import reset.reset.Models.document.Documento;
import reset.reset.Services.document.DocumentoService;
import reset.reset.dto.filter.DocumentoFilter;
import reset.reset.dto.projection.DocumentoResumo;
import reset.reset.dto.request.DocumentoRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documento", description = "Document management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE', 'CONTABILISTA')")
public class DocumentoController extends BaseController {

    private final DocumentoService documentoService;

    @PostMapping
    @Operation(summary = "Create a new document")
    @PreAuthorize("hasPermission('DOCUMENTO_CREATE')")
    public ResponseEntity<ApiResponse<Documento>> create(@Valid @RequestBody DocumentoRequest request) {
        log.info("Creating new documento");
        Documento documento = request.toEntity();
        Documento saved = documentoService.save(documento);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing document")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<Documento>> update(@PathVariable Long id,
                                                         @Valid @RequestBody DocumentoRequest request) {
        log.info("Updating documento with id: {}", id);
        Documento documento = request.toEntity();
        documento.setId(id);
        Documento updated = documentoService.update(id, documento);
        return success(updated, "Documento updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Documento>> findById(@PathVariable Long id) {
        Documento documento = documentoService.findByIdOrThrow(id);
        return success(documento);
    }

    @GetMapping
    @Operation(summary = "Get all documents with pagination and filtering")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<Documento>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            DocumentoFilter filter) {
        Page<Documento> documentos = documentoService.filter(filter);
        return success(documentos);
    }

    @GetMapping("/empresa/{empresaId}/resumo")
    @Operation(summary = "Get document summary by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<DocumentoResumo>>> findResumoByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentoResumo> documentos = documentoService.findDocumentoResumoByEmpresaId(empresaId, pageable);
        return success(documentos);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Get documents by client")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<Documento>>> findByCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Documento> documentos = documentoService.findByClienteId(clienteId, pageable);
        return success(documentos);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Update document status")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<Documento>> updateEstado(@PathVariable Long id,
                                                               @RequestParam String estado) {
        Documento documento = documentoService.mudarEstado(id, estado);
        return success(documento, "Estado updated successfully");
    }

    @GetMapping("/empresa/{empresaId}/total-periodo")
    @Operation(summary = "Get total document value by company and period")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalByEmpresaAndPeriodo(
            @PathVariable Long empresaId,
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim) {
        BigDecimal total = documentoService.sumTotalByEmpresaAndPeriodo(empresaId, inicio, fim);
        return success(total);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document")
    @PreAuthorize("hasPermission('DOCUMENTO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentoService.deleteById(id);
        return noContent();
    }
}
