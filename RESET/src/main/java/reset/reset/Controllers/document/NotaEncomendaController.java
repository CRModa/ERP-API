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
import reset.reset.Models.document.Tipos.NotaEncomenda;
import reset.reset.Services.document.NotaEncomendaService;
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
    public ResponseEntity<ApiResponse<NotaEncomenda>> create(@Valid @RequestBody NotaEncomendaRequest request) {
        log.info("Creating new nota encomenda");
        NotaEncomenda nota = request.toEntity();
        NotaEncomenda saved = notaEncomendaService.save(nota);
        return created(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<NotaEncomenda>> findById(@PathVariable Long id) {
        NotaEncomenda nota = notaEncomendaService.findByIdOrThrow(id);
        return success(nota);
    }

    @GetMapping
    @Operation(summary = "Get all purchase orders with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<NotaEncomenda>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotaEncomenda> notas = notaEncomendaService.findAll(pageable);
        return success(notas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get purchase orders by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<NotaEncomenda>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotaEncomenda> notas = notaEncomendaService.findByEmpresaId(empresaId, pageable);
        return success(notas);
    }

    @GetMapping("/cotacao/{cotacaoId}")
    @Operation(summary = "Get purchase orders by quotation")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<NotaEncomenda>>> findByCotacao(@PathVariable Long cotacaoId) {
        List<NotaEncomenda> notas = notaEncomendaService.findByCotacaoId(cotacaoId);
        return success(notas);
    }

    @GetMapping("/atrasadas")
    @Operation(summary = "Get overdue purchase orders")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<NotaEncomenda>>> findAtrasadas() {
        List<NotaEncomenda> notas = notaEncomendaService.findEncomendasAtrasadas();
        return success(notas);
    }

    @PatchMapping("/{id}/processar")
    @Operation(summary = "Send purchase order for processing")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaEncomenda>> processar(@PathVariable Long id) {
        NotaEncomenda nota = notaEncomendaService.enviarParaProcessamento(id);
        return success(nota, "Nota encomenda sent for processing");
    }

    @PatchMapping("/{id}/enviar")
    @Operation(summary = "Mark purchase order as shipped")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaEncomenda>> enviar(@PathVariable Long id) {
        NotaEncomenda nota = notaEncomendaService.marcarComoEnviado(id);
        return success(nota, "Nota encomenda marked as shipped");
    }

    @PatchMapping("/{id}/entregar")
    @Operation(summary = "Mark purchase order as delivered")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<NotaEncomenda>> entregar(@PathVariable Long id) {
        NotaEncomenda nota = notaEncomendaService.marcarComoEntregue(id);
        return success(nota, "Nota encomenda marked as delivered");
    }
}
