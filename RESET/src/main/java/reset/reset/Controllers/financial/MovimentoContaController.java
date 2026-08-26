package reset.reset.Controllers.financial;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Controllers.base.BaseController;
import reset.reset.Services.financial.MovimentoContaService;
import reset.reset.dto.financial.MovimentoContaDTO;
import reset.reset.dto.financial.MovimentoResumoDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/movimentos-conta")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movimento Conta", description = "Account movement management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTABILISTA', 'GERENTE')")
public class MovimentoContaController extends BaseController {

    private final MovimentoContaService movimentoContaService;

    @GetMapping("/{id}")
    @Operation(summary = "Get account movement by ID")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<MovimentoContaDTO>> findById(@PathVariable Long id) {
        MovimentoContaDTO movimento = movimentoContaService.findByIdDTO(id);
        return success(movimento);
    }

    @GetMapping
    @Operation(summary = "Get all account movements with pagination and filters")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<Page<MovimentoContaDTO>>> findAll(
            @PageableDefault(size = 20, sort = "data", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        Page<MovimentoContaDTO> movimentos = movimentoContaService.findByFiltrosPaginadoDTO(
                contaId, tipo, dataInicio, dataFim, pageable);
        return success(movimentos);
    }

    @GetMapping("/conta/{contaId}")
    @Operation(summary = "Get account movements by account")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<Page<MovimentoContaDTO>>> findByConta(
            @PathVariable Long contaId,
            @PageableDefault(size = 20, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MovimentoContaDTO> movimentos = movimentoContaService.findByContaIdDTO(contaId, pageable);
        return success(movimentos);
    }

    @GetMapping("/documento/{documentoId}")
    @Operation(summary = "Get account movements by document")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<MovimentoContaDTO>>> findByDocumento(@PathVariable Long documentoId) {
        List<MovimentoContaDTO> movimentos = movimentoContaService.findByDocumentoIdDTO(documentoId);
        return success(movimentos);
    }

    @GetMapping("/periodo")
    @Operation(summary = "Get account movements by date range")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<MovimentoContaDTO>>> findByPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        List<MovimentoContaDTO> movimentos = movimentoContaService.findByDataBetweenDTO(inicio, fim);
        return success(movimentos);
    }

    @GetMapping("/conta/{contaId}/sum/{tipo}")
    @Operation(summary = "Sum account movements by account and type")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> sumByContaAndTipo(
            @PathVariable Long contaId,
            @PathVariable String tipo) {
        BigDecimal sum = movimentoContaService.sumByContaIdAndTipoDTO(contaId, tipo);
        return success(sum);
    }

    @GetMapping("/resumo")
    @Operation(summary = "Get account movements summary")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<MovimentoResumoDTO>> getResumo(
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        // Buscar movimentos com os filtros
        List<MovimentoContaDTO> movimentos = movimentoContaService.findByFiltrosDTO(
                contaId, null, dataInicio, dataFim);

        // Calcular totais
        BigDecimal totalEntradas = movimentos.stream()
                .filter(m -> "ENTRADA".equals(m.getTipo()))
                .map(MovimentoContaDTO::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = movimentos.stream()
                .filter(m -> "SAIDA".equals(m.getTipo()))
                .map(MovimentoContaDTO::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MovimentoResumoDTO resumo = MovimentoResumoDTO.builder()
                .totalMovimentos((long) movimentos.size())
                .totalEntradas(totalEntradas)
                .totalSaidas(totalSaidas)
                .saldo(totalEntradas.subtract(totalSaidas))
                .build();

        return success(resumo);
    }
}