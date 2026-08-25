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
import reset.reset.Services.document.NotaEncomendaService;
import reset.reset.dto.document.NotaEncomendaDTO;
import reset.reset.dto.request.NotaEncomendaRequest;

import java.util.List;

@RestController
@RequestMapping("/notas-encomenda")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Nota Encomenda", description = "Purchase order management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class NotaEncomendaController extends BaseController {

    private final NotaEncomendaService notaEncomendaService;

    @PostMapping
    @Operation(summary = "Create a new purchase order")
    @PreAuthorize("hasPermission('DOCUMENTO_CREATE')")
    public ResponseEntity<ApiResponse<NotaEncomendaDTO>> create(@Valid @RequestBody NotaEncomendaRequest request) {
        log.info("Creating new nota encomenda");
        NotaEncomendaDTO dto = notaEncomendaService.createNotaEncomenda(request);
        return created(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<NotaEncomendaDTO>> findById(@PathVariable Long id) {
        NotaEncomendaDTO dto = notaEncomendaService.findByIdDTO(id);
        return success(dto);
    }

    @GetMapping
    @Operation(summary = "Get all purchase orders with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<NotaEncomendaDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotaEncomendaDTO> notas = notaEncomendaService.findAllDTO(pageable);
        return success(notas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get purchase orders by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<NotaEncomendaDTO>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotaEncomendaDTO> notas = notaEncomendaService.findByEmpresaIdDTO(empresaId, pageable);
        return success(notas);
    }

    @GetMapping("/cotacao/{cotacaoId}")
    @Operation(summary = "Get purchase orders by quotation")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<NotaEncomendaDTO>>> findByCotacao(@PathVariable Long cotacaoId) {
        List<NotaEncomendaDTO> notas = notaEncomendaService.findByCotacaoIdDTO(cotacaoId);
        return success(notas);
    }

    @GetMapping("/atrasadas")
    @Operation(summary = "Get overdue purchase orders")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<NotaEncomendaDTO>>> findAtrasadas() {
        List<NotaEncomendaDTO> notas = notaEncomendaService.findEncomendasAtrasadasDTO();
        return success(notas);
    }

    @PatchMapping("/{id}/processar")
    @Operation(summary = "Send purchase order for processing")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaEncomendaDTO>> processar(@PathVariable Long id) {
        NotaEncomendaDTO dto = notaEncomendaService.enviarParaProcessamentoDTO(id);
        return success(dto, "Nota encomenda sent for processing");
    }

    @PatchMapping("/{id}/enviar")
    @Operation(summary = "Mark purchase order as shipped")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaEncomendaDTO>> enviar(@PathVariable Long id) {
        NotaEncomendaDTO dto = notaEncomendaService.marcarComoEnviadoDTO(id);
        return success(dto, "Nota encomenda marked as shipped");
    }

    @PatchMapping("/{id}/entregar")
    @Operation(summary = "Mark purchase order as delivered")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaEncomendaDTO>> entregar(@PathVariable Long id) {
        NotaEncomendaDTO dto = notaEncomendaService.marcarComoEntregueDTO(id);
        return success(dto, "Nota encomenda marked as delivered");
    }
}