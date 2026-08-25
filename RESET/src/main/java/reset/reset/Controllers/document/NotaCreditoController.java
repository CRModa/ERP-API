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
import reset.reset.Services.document.NotaCreditoService;
import reset.reset.dto.document.NotaCreditoDTO;
import reset.reset.dto.request.NotaCreditoRequest;

@RestController
@RequestMapping("/notas-credito")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Nota Crédito", description = "Credit note management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class NotaCreditoController extends BaseController {

    private final NotaCreditoService notaCreditoService;

    @PostMapping
    @Operation(summary = "Create a new credit note")
    @PreAuthorize("hasPermission('DOCUMENTO_CREATE')")
    public ResponseEntity<ApiResponse<NotaCreditoDTO>> create(@Valid @RequestBody NotaCreditoRequest request) {
        log.info("Creating new nota credito");
        NotaCreditoDTO dto = notaCreditoService.createNotaCredito(request);
        return created(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get credit note by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<NotaCreditoDTO>> findById(@PathVariable Long id) {
        NotaCreditoDTO dto = notaCreditoService.findByIdDTO(id);
        return success(dto);
    }

    @GetMapping
    @Operation(summary = "Get all credit notes with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<NotaCreditoDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotaCreditoDTO> notas = notaCreditoService.findAllDTO(pageable);
        return success(notas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get credit notes by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<NotaCreditoDTO>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotaCreditoDTO> notas = notaCreditoService.findByEmpresaIdDTO(empresaId, pageable);
        return success(notas);
    }

    @GetMapping("/documento-origem/{documentoOrigemId}")
    @Operation(summary = "Get credit note by original document")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<NotaCreditoDTO>> findByDocumentoOrigem(@PathVariable Long documentoOrigemId) {
        NotaCreditoDTO dto = notaCreditoService.findByDocumentoOrigemIdDTO(documentoOrigemId);
        return success(dto);
    }

    @PatchMapping("/{id}/aprovar")
    @Operation(summary = "Approve credit note")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaCreditoDTO>> aprovar(@PathVariable Long id) {
        NotaCreditoDTO dto = notaCreditoService.aprovarNotaCreditoDTO(id);
        return success(dto, "Credit note approved");
    }

    @PatchMapping("/{id}/rejeitar")
    @Operation(summary = "Reject credit note")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaCreditoDTO>> rejeitar(@PathVariable Long id,
                                                                @RequestParam String motivo) {
        NotaCreditoDTO dto = notaCreditoService.rejeitarNotaCreditoDTO(id, motivo);
        return success(dto, "Credit note rejected");
    }
}
