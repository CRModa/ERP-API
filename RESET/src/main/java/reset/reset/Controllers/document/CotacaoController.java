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
import reset.reset.Models.document.Tipos.Cotacao;
import reset.reset.Services.document.CotacaoService;
import reset.reset.dto.request.CotacaoRequest;

import java.util.List;

@RestController
@RequestMapping("/cotacoes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cotação", description = "Quotation management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class CotacaoController extends BaseController {

    private final CotacaoService cotacaoService;

    @PostMapping
    @Operation(summary = "Create a new quotation")
    @PreAuthorize("hasPermission('DOCUMENTO_CREATE')")
    public ResponseEntity<ApiResponse<Cotacao>> create(@Valid @RequestBody CotacaoRequest request) {
        log.info("Creating new cotacao");
        Cotacao cotacao = request.toEntity();
        Cotacao saved = cotacaoService.save(cotacao);
        return created(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quotation by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Cotacao>> findById(@PathVariable Long id) {
        Cotacao cotacao = cotacaoService.findByIdOrThrow(id);
        return success(cotacao);
    }

    @GetMapping
    @Operation(summary = "Get all quotations with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<Cotacao>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Cotacao> cotacoes = cotacaoService.findAll(pageable);
        return success(cotacoes);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get quotations by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<Cotacao>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Cotacao> cotacoes = cotacaoService.findByEmpresaId(empresaId, pageable);
        return success(cotacoes);
    }

    @GetMapping("/pendentes")
    @Operation(summary = "Get pending quotations")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<Cotacao>>> findPendentes() {
        List<Cotacao> cotacoes = cotacaoService.findCotacoesPendentes();
        return success(cotacoes);
    }

    @GetMapping("/aprovadas")
    @Operation(summary = "Get approved quotations")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<Cotacao>>> findAprovadas() {
        List<Cotacao> cotacoes = cotacaoService.findCotacoesAprovadas();
        return success(cotacoes);
    }

    @GetMapping("/expiradas")
    @Operation(summary = "Get expired quotations")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<Cotacao>>> findExpiradas() {
        List<Cotacao> cotacoes = cotacaoService.findCotacoesExpiradas();
        return success(cotacoes);
    }

    @PatchMapping("/{id}/aprovar")
    @Operation(summary = "Approve quotation")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<Cotacao>> aprovar(@PathVariable Long id) {
        Cotacao cotacao = cotacaoService.aprovarCotacao(id);
        return success(cotacao, "Cotação approved");
    }

    @PatchMapping("/{id}/rejeitar")
    @Operation(summary = "Reject quotation")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<Cotacao>> rejeitar(@PathVariable Long id,
                                                         @RequestParam String motivo) {
        Cotacao cotacao = cotacaoService.rejeitarCotacao(id, motivo);
        return success(cotacao, "Cotação rejected");
    }
}
