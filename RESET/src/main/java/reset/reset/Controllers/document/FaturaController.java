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
import reset.reset.Models.document.Tipos.Fatura;
import reset.reset.Services.document.FaturaService;

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
    public ResponseEntity<ApiResponse<Fatura>> findById(@PathVariable Long id) {
        Fatura fatura = faturaService.findByIdOrThrow(id);
        return success(fatura);
    }

    @GetMapping
    @Operation(summary = "Get all invoices with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<Fatura>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Fatura> faturas = faturaService.findAll(pageable);
        return success(faturas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get invoices by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<Fatura>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Fatura> faturas = faturaService.findByEmpresaId(empresaId, pageable);
        return success(faturas);
    }

    @GetMapping("/nao-pagas")
    @Operation(summary = "Get unpaid invoices")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<Fatura>>> findNaoPagas() {
        List<Fatura> faturas = faturaService.findFaturasNaoPagas();
        return success(faturas);
    }

    @GetMapping("/vencidas")
    @Operation(summary = "Get overdue invoices")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<Fatura>>> findVencidas() {
        List<Fatura> faturas = faturaService.findFaturasVencidas();
        return success(faturas);
    }

    @GetMapping("/cliente/{clienteId}/nao-pagas")
    @Operation(summary = "Get unpaid invoices by client")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<Fatura>>> findNaoPagasByCliente(@PathVariable Long clienteId) {
        List<Fatura> faturas = faturaService.findFaturasNaoPagasByCliente(clienteId);
        return success(faturas);
    }

    @PatchMapping("/{id}/pagar")
    @Operation(summary = "Mark invoice as paid")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<Fatura>> marcarComoPaga(@PathVariable Long id) {
        Fatura fatura = faturaService.marcarComoPaga(id);
        return success(fatura, "Fatura marked as paid");
    }

    @PatchMapping("/{id}/nao-pagar")
    @Operation(summary = "Mark invoice as unpaid")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<Fatura>> marcarComoNaoPaga(@PathVariable Long id) {
        Fatura fatura = faturaService.marcarComoNaoPaga(id);
        return success(fatura, "Fatura marked as unpaid");
    }
}
