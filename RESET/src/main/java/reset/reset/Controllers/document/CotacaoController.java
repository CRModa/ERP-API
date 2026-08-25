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
import reset.reset.Services.document.CotacaoService;
import reset.reset.dto.document.CotacaoDTO;
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
    public ResponseEntity<ApiResponse<CotacaoDTO>> create(@Valid @RequestBody CotacaoRequest request) {
        log.info("Creating new cotacao");
        return created(cotacaoService.createCotacao(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quotation by ID")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<CotacaoDTO>> findById(@PathVariable Long id) {
        CotacaoDTO cotacao = cotacaoService.findByIdDTO(id);
        return success(cotacao);
    }

    @GetMapping
    @Operation(summary = "Get all quotations with pagination")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<CotacaoDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CotacaoDTO> cotacoes = cotacaoService.findAllDTO(pageable);
        return success(cotacoes);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get quotations by company")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<Page<CotacaoDTO>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CotacaoDTO> cotacoes = cotacaoService.findByEmpresaIdDTO(empresaId, pageable);
        return success(cotacoes);
    }

    @GetMapping("/pendentes")
    @Operation(summary = "Get pending quotations")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<CotacaoDTO>>> findPendentes() {
        List<CotacaoDTO> cotacoes = cotacaoService.findCotacoesPendentesDTO();
        return success(cotacoes);
    }

    @GetMapping("/aprovadas")
    @Operation(summary = "Get approved quotations")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<CotacaoDTO>>> findAprovadas() {
        List<CotacaoDTO> cotacoes = cotacaoService.findCotacoesAprovadasDTO();
        return success(cotacoes);
    }

    @GetMapping("/expiradas")
    @Operation(summary = "Get expired quotations")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<List<CotacaoDTO>>> findExpiradas() {
        List<CotacaoDTO> cotacoes = cotacaoService.findCotacoesExpiradasDTO();
        return success(cotacoes);
    }

    @PatchMapping("/{id}/aprovar")
    @Operation(summary = "Approve quotation")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<CotacaoDTO>> aprovar(@PathVariable Long id) {
        CotacaoDTO cotacao = cotacaoService.aprovarCotacaoDTO(id);
        return success(cotacao, "Cotação approved");
    }

    @PatchMapping("/{id}/rejeitar")
    @Operation(summary = "Reject quotation")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<CotacaoDTO>> rejeitar(@PathVariable Long id,
                                                            @RequestParam String motivo) {
        CotacaoDTO cotacao = cotacaoService.rejeitarCotacaoDTO(id, motivo);
        return success(cotacao, "Cotação rejected");
    }

    @PatchMapping("/{id}/converter")
    @Operation(summary = "Convert quotation to proforma invoice")
    @PreAuthorize("hasPermission('DOCUMENTO_UPDATE')")
    public ResponseEntity<ApiResponse<CotacaoDTO>> converter(@PathVariable Long id) {
        CotacaoDTO cotacao = cotacaoService.converterParaFaturaProformaDTO(id);
        return success(cotacao, "Cotação converted to proforma invoice");
    }
}