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
import reset.reset.Services.document.FaturaProformaService;
import reset.reset.dto.document.FaturaProformaDTO;
import reset.reset.dto.request.FaturaProformaRequest;

@RestController
@RequestMapping("/faturas-proforma")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fatura Proforma", description = "Proforma invoice management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class FaturaProformaController extends BaseController {

    private final FaturaProformaService faturaProformaService;

    @PostMapping
    @Operation(summary = "Create a new proforma invoice")
    @PreAuthorize("hasPermission('DOCUMENTO_CREATE')")
    public ResponseEntity<ApiResponse<FaturaProformaDTO>> create(@Valid @RequestBody FaturaProformaRequest request) {
        log.info("Creating new fatura proforma");
        FaturaProformaDTO dto = faturaProformaService.createProforma(request);
        return created(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get proforma invoice by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<FaturaProformaDTO>> findById(@PathVariable Long id) {
        FaturaProformaDTO dto = faturaProformaService.findByIdDTO(id);
        return success(dto);
    }

    @GetMapping
    @Operation(summary = "Get all proforma invoices with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<FaturaProformaDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<FaturaProformaDTO> faturas = faturaProformaService.findAllDTO(pageable);
        return success(faturas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get proforma invoices by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<FaturaProformaDTO>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FaturaProformaDTO> faturas = faturaProformaService.findByEmpresaIdDTO(empresaId, pageable);
        return success(faturas);
    }

    @PatchMapping("/{id}/aprovar")
    @Operation(summary = "Approve proforma invoice")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<FaturaProformaDTO>> aprovar(@PathVariable Long id) {
        FaturaProformaDTO dto = faturaProformaService.aprovarFaturaProformaDTO(id);
        return success(dto, "Proforma invoice approved");
    }

    @PatchMapping("/{id}/converter")
    @Operation(summary = "Convert proforma invoice to invoice")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<FaturaProformaDTO>> converter(@PathVariable Long id,
                                                                    @RequestParam Long faturaId) {
        FaturaProformaDTO dto = faturaProformaService.converterParaFaturaDTO(id, faturaId);
        return success(dto, "Proforma invoice converted to invoice");
    }
}