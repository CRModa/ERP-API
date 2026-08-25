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
import reset.reset.Services.document.NotaDebitoService;
import reset.reset.dto.document.NotaDebitoDTO;
import reset.reset.dto.request.NotaDebitoRequest;

@RestController
@RequestMapping("/notas-debito")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Nota Débito", description = "Debit note management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class NotaDebitoController extends BaseController {

    private final NotaDebitoService notaDebitoService;

    @PostMapping
    @Operation(summary = "Create a new debit note")
    @PreAuthorize("hasPermission('DOCUMENTO_CREATE')")
    public ResponseEntity<ApiResponse<NotaDebitoDTO>> create(@Valid @RequestBody NotaDebitoRequest request) {
        log.info("Creating new nota debito");
        NotaDebitoDTO dto = notaDebitoService.createNotaDebito(request);
        return created(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get debit note by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<NotaDebitoDTO>> findById(@PathVariable Long id) {
        NotaDebitoDTO dto = notaDebitoService.findByIdDTO(id);
        return success(dto);
    }

    @GetMapping
    @Operation(summary = "Get all debit notes with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<NotaDebitoDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotaDebitoDTO> notas = notaDebitoService.findAllDTO(pageable);
        return success(notas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get debit notes by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<NotaDebitoDTO>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotaDebitoDTO> notas = notaDebitoService.findByEmpresaIdDTO(empresaId, pageable);
        return success(notas);
    }

    @GetMapping("/documento-origem/{documentoOrigemId}")
    @Operation(summary = "Get debit note by original document")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<NotaDebitoDTO>> findByDocumentoOrigem(@PathVariable Long documentoOrigemId) {
        NotaDebitoDTO dto = notaDebitoService.findByDocumentoOrigemIdDTO(documentoOrigemId);
        return success(dto);
    }

    @PatchMapping("/{id}/aprovar")
    @Operation(summary = "Approve debit note")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaDebitoDTO>> aprovar(@PathVariable Long id) {
        NotaDebitoDTO dto = notaDebitoService.aprovarNotaDebitoDTO(id);
        return success(dto, "Debit note approved");
    }

    @PatchMapping("/{id}/rejeitar")
    @Operation(summary = "Reject debit note")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaDebitoDTO>> rejeitar(@PathVariable Long id,
                                                               @RequestParam String motivo) {
        NotaDebitoDTO dto = notaDebitoService.rejeitarNotaDebitoDTO(id, motivo);
        return success(dto, "Debit note rejected");
    }
}