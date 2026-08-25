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
import reset.reset.Services.document.GuiaTransporteService;
import reset.reset.dto.document.GuiaTransporteDTO;
import reset.reset.dto.request.GuiaTransporteRequest;

@RestController
@RequestMapping("/guias-transporte")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Guia Transporte", description = "Transport guide management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class GuiaTransporteController extends BaseController {

    private final GuiaTransporteService guiaTransporteService;

    @PostMapping
    @Operation(summary = "Create a new transport guide")
    @PreAuthorize("hasPermission('DOCUMENTO_CREATE')")
    public ResponseEntity<ApiResponse<GuiaTransporteDTO>> create(@Valid @RequestBody GuiaTransporteRequest request) {
        log.info("Creating new guia transporte");
        GuiaTransporteDTO dto = guiaTransporteService.createGuiaTransporte(request);
        return created(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transport guide by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<GuiaTransporteDTO>> findById(@PathVariable Long id) {
        GuiaTransporteDTO dto = guiaTransporteService.findByIdDTO(id);
        return success(dto);
    }

    @GetMapping
    @Operation(summary = "Get all transport guides with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<GuiaTransporteDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<GuiaTransporteDTO> guias = guiaTransporteService.findAllDTO(pageable);
        return success(guias);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get transport guides by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<GuiaTransporteDTO>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GuiaTransporteDTO> guias = guiaTransporteService.findByEmpresaIdDTO(empresaId, pageable);
        return success(guias);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Get transport guides by status")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<GuiaTransporteDTO>>> findByEstado(
            @PathVariable String estado,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GuiaTransporteDTO> guias = guiaTransporteService.findByEstadoDTO(estado, pageable);
        return success(guias);
    }

    @PatchMapping("/{id}/iniciar")
    @Operation(summary = "Start transport")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<GuiaTransporteDTO>> iniciar(@PathVariable Long id) {
        GuiaTransporteDTO dto = guiaTransporteService.iniciarTransporteDTO(id);
        return success(dto, "Transport started");
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Finalize transport")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<GuiaTransporteDTO>> finalizar(@PathVariable Long id) {
        GuiaTransporteDTO dto = guiaTransporteService.finalizarTransporteDTO(id);
        return success(dto, "Transport finalized");
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancel transport")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<GuiaTransporteDTO>> cancelar(@PathVariable Long id,
                                                                   @RequestParam(required = false) String observacao) {
        GuiaTransporteDTO dto = guiaTransporteService.cancelarTransporteDTO(id, observacao);
        return success(dto, "Transport cancelled");
    }
}