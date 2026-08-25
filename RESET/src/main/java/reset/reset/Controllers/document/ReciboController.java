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
import reset.reset.Services.document.ReciboService;
import reset.reset.dto.document.ReciboDTO;
import reset.reset.dto.request.ReciboRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/recibos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recibo", description = "Receipt management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class ReciboController extends BaseController {

    private final ReciboService reciboService;

    @PostMapping
    @Operation(summary = "Create a new receipt")
    @PreAuthorize("hasPermission('DOCUMENTO_CREATE')")
    public ResponseEntity<ApiResponse<ReciboDTO>> create(@Valid @RequestBody ReciboRequest request) {
        log.info("Creating new recibo");
        ReciboDTO dto = reciboService.createRecibo(request);
        return created(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get receipt by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<ReciboDTO>> findById(@PathVariable Long id) {
        ReciboDTO dto = reciboService.findByIdDTO(id);
        return success(dto);
    }

    @GetMapping
    @Operation(summary = "Get all receipts with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<ReciboDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReciboDTO> recibos = reciboService.findAllDTO(pageable);
        return success(recibos);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get receipts by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<ReciboDTO>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReciboDTO> recibos = reciboService.findByEmpresaIdDTO(empresaId, pageable);
        return success(recibos);
    }

    @GetMapping("/forma-pagamento/{formaPagamento}")
    @Operation(summary = "Get receipts by payment method")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<ReciboDTO>>> findByFormaPagamento(@PathVariable String formaPagamento) {
        List<ReciboDTO> recibos = reciboService.findByFormaPagamentoDTO(formaPagamento);
        return success(recibos);
    }

    @GetMapping("/periodo")
    @Operation(summary = "Get receipts by date range")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<ReciboDTO>>> findByPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {
        List<ReciboDTO> recibos = reciboService.findByDataPagamentoBetweenDTO(inicio, fim);
        return success(recibos);
    }

    @GetMapping("/referencia/{referencia}")
    @Operation(summary = "Get receipts by payment reference")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<ReciboDTO>>> findByReferencia(@PathVariable String referencia) {
        List<ReciboDTO> recibos = reciboService.findByReferenciaPagamentoDTO(referencia);
        return success(recibos);
    }
}