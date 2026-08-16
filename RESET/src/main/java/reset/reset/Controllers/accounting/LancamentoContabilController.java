package reset.reset.Controllers.accounting;

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
import reset.reset.Models.accounting.LancamentoContabil;
import reset.reset.Models.accounting.LancamentoContabilLinha;
import reset.reset.Services.accounting.LancamentoContabilLinhaService;
import reset.reset.Services.accounting.LancamentoContabilService;
import reset.reset.dto.request.LancamentoContabilRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/lancamentos-contabeis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lançamento Contábil", description = "Accounting entry management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTABILISTA')")
public class LancamentoContabilController extends BaseController {

    private final LancamentoContabilService lancamentoService;
    private final LancamentoContabilLinhaService linhaService;

    @PostMapping
    @Operation(summary = "Create a new accounting entry")
    @PreAuthorize("hasPermission('CONTABIL_CREATE')")
    public ResponseEntity<ApiResponse<LancamentoContabil>> create(@Valid @RequestBody LancamentoContabilRequest request) {
        log.info("Creating new lancamento contabil");
        LancamentoContabil lancamento = request.toEntity();
        LancamentoContabil saved = lancamentoService.save(lancamento);
        return created(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accounting entry by ID")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<LancamentoContabil>> findById(@PathVariable Long id) {
        LancamentoContabil lancamento = lancamentoService.findByIdOrThrow(id);
        return success(lancamento);
    }

    @GetMapping
    @Operation(summary = "Get all accounting entries with pagination")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<Page<LancamentoContabil>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LancamentoContabil> lancamentos = lancamentoService.findAll(pageable);
        return success(lancamentos);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get accounting entries by company")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<Page<LancamentoContabil>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<LancamentoContabil> lancamentos = lancamentoService.findByEmpresaId(empresaId, pageable);
        return success(lancamentos);
    }

    @GetMapping("/diario/{diarioId}")
    @Operation(summary = "Get accounting entries by journal")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<List<LancamentoContabil>>> findByDiario(@PathVariable Long diarioId) {
        List<LancamentoContabil> lancamentos = lancamentoService.findByDiarioId(diarioId);
        return success(lancamentos);
    }

    @GetMapping("/documento/{documentoId}")
    @Operation(summary = "Get accounting entries by document")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<List<LancamentoContabil>>> findByDocumento(@PathVariable Long documentoId) {
        List<LancamentoContabil> lancamentos = lancamentoService.findByDocumentoId(documentoId);
        return success(lancamentos);
    }

    @GetMapping("/numero/{numeroLancamento}")
    @Operation(summary = "Get accounting entry by number")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<LancamentoContabil>> findByNumero(@PathVariable String numeroLancamento) {
        LancamentoContabil lancamento = lancamentoService.findByNumeroLancamento(numeroLancamento);
        return success(lancamento);
    }

    @GetMapping("/periodo")
    @Operation(summary = "Get accounting entries by date range")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<List<LancamentoContabil>>> findByPeriodo(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim) {
        List<LancamentoContabil> lancamentos = lancamentoService.findByDataLancamentoBetween(inicio, fim);
        return success(lancamentos);
    }

    @GetMapping("/{id}/linhas")
    @Operation(summary = "Get lines of an accounting entry")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<List<LancamentoContabilLinha>>> findLinhasByLancamento(@PathVariable Long id) {
        List<LancamentoContabilLinha> linhas = linhaService.findByLancamentoId(id);
        return success(linhas);
    }

    @GetMapping("/{id}/total-debito")
    @Operation(summary = "Get total debit of an accounting entry")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalDebito(@PathVariable Long id) {
        BigDecimal total = linhaService.sumDebitosByLancamentoId(id);
        return success(total);
    }

    @GetMapping("/{id}/total-credito")
    @Operation(summary = "Get total credit of an accounting entry")
    @PreAuthorize("hasPermission('CONTABIL_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalCredito(@PathVariable Long id) {
        BigDecimal total = linhaService.sumCreditosByLancamentoId(id);
        return success(total);
    }
}
