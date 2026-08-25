package reset.reset.Controllers.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import reset.reset.Services.document.FaturaService;
import reset.reset.dto.document.FaturaDTO;

import java.util.List;

@RestController
@RequestMapping("/faturas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fatura", description = "Invoice management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class FaturaController extends BaseController {

    private final FaturaService faturaService;

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<FaturaDTO>> findById(@PathVariable Long id) {
        FaturaDTO fatura = faturaService.findByIdDTO(id);
        return success(fatura);
    }

    @GetMapping
    @Operation(summary = "Get all invoices with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<FaturaDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<FaturaDTO> faturas = faturaService.findAllDTO(pageable);
        return success(faturas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get invoices by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<FaturaDTO>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FaturaDTO> faturas = faturaService.findByEmpresaIdDTO(empresaId, pageable);
        return success(faturas);
    }

    @GetMapping("/nao-pagas")
    @Operation(summary = "Get unpaid invoices")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<FaturaDTO>>> findNaoPagas() {
        List<FaturaDTO> faturas = faturaService.findFaturasNaoPagasDTO();
        return success(faturas);
    }

    @GetMapping("/vencidas")
    @Operation(summary = "Get overdue invoices")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<FaturaDTO>>> findVencidas() {
        List<FaturaDTO> faturas = faturaService.findFaturasVencidasDTO();
        return success(faturas);
    }

    @GetMapping("/cliente/{clienteId}/nao-pagas")
    @Operation(summary = "Get unpaid invoices by client")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<FaturaDTO>>> findNaoPagasByCliente(@PathVariable Long clienteId) {
        List<FaturaDTO> faturas = faturaService.findFaturasNaoPagasByClienteDTO(clienteId);
        return success(faturas);
    }

    @PatchMapping("/{id}/pagar")
    @Operation(summary = "Mark invoice as paid")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<FaturaDTO>> marcarComoPaga(@PathVariable Long id) {
        FaturaDTO fatura = faturaService.marcarComoPagaDTO(id);
        return success(fatura, "Fatura marked as paid");
    }

    @PatchMapping("/{id}/nao-pagar")
    @Operation(summary = "Mark invoice as unpaid")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<FaturaDTO>> marcarComoNaoPaga(@PathVariable Long id) {
        FaturaDTO fatura = faturaService.marcarComoNaoPagaDTO(id);
        return success(fatura, "Fatura marked as unpaid");
    }
}