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
import reset.reset.dto.document.DocumentoDTO;
import reset.reset.dto.document.DocumentoResumoDTO;
import reset.reset.dto.filter.DocumentoFilter;
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
    public ResponseEntity<ApiResponse<DocumentoDTO>> create(@Valid @RequestBody DocumentoRequest request) {
        log.info("Creating new documento");
        Documento documento = request.toEntity();
        Documento saved = documentoService.save(documento);
        return created(DocumentoDTO.fromEntity(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing document")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<DocumentoDTO>> update(@PathVariable Long id,
                                                            @Valid @RequestBody DocumentoRequest request) {
        log.info("Updating documento with id: {}", id);
        Documento documento = request.toEntity();
        documento.setId(id);
        Documento updated = documentoService.update(id, documento);
        return success(DocumentoDTO.fromEntity(updated), "Documento updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<DocumentoDTO>> findById(@PathVariable Long id) {
        Documento documento = documentoService.findByIdOrThrow(id);
        return success(DocumentoDTO.fromEntity(documento));
    }

    @GetMapping
    @Operation(summary = "Get all documents with pagination and filtering")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<DocumentoDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            DocumentoFilter filter) {
        Page<DocumentoDTO> documentos = documentoService.filterDTO(filter);
        return success(documentos);
    }

    @GetMapping("/empresa/{empresaId}/resumo")
    @Operation(summary = "Get document summary by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<DocumentoResumoDTO>>> findResumoByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentoResumoDTO> documentos = documentoService.findDocumentoResumoByEmpresaIdDTO(empresaId, pageable);
        return success(documentos);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Get documents by client")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<DocumentoDTO>>> findByCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentoDTO> documentos = documentoService.findByClienteIdDTO(clienteId, pageable);
        return success(documentos);
    }

    @GetMapping("/tipo/{tipoId}")
    @Operation(summary = "Get documents by type")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<DocumentoDTO>>> findByTipo(
            @PathVariable Long tipoId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentoDTO> documentos = documentoService.findByTipoIdDTO(tipoId, pageable);
        return success(documentos);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Get documents by status")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<DocumentoDTO>>> findByEstado(
            @PathVariable String estado,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DocumentoDTO> documentos = documentoService.findByEstadoDTO(estado, pageable);
        return success(documentos);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Update document status")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<DocumentoDTO>> updateEstado(@PathVariable Long id,
                                                                  @RequestParam String estado) {
        Documento documento = documentoService.mudarEstado(id, estado);
        return success(DocumentoDTO.fromEntity(documento), "Estado updated successfully");
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

    @GetMapping("/empresa/{empresaId}/count-periodo")
    @Operation(summary = "Get document count by company and period")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Long>> getCountByEmpresaAndPeriodo(
            @PathVariable Long empresaId,
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim) {
        long count = documentoService.countByEmpresaAndPeriodo(empresaId, inicio, fim);
        return success(count);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document")
    @PreAuthorize("hasPermission('DOCUMENTO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentoService.deleteById(id);
        return noContent();
    }
}