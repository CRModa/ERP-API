package reset.reset.Controllers.financial;

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
import reset.reset.Models.financial.ContaCorrente;
import reset.reset.Services.financial.ContaCorrenteService;
import reset.reset.dto.filter.ContaCorrenteFilter;
import reset.reset.dto.request.ContaCorrenteRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/conta-corrente")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conta Corrente", description = "Current account management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTABILISTA', 'GERENTE')")
public class ContaCorrenteController extends BaseController {

    private final ContaCorrenteService contaCorrenteService;

    @PostMapping
    @Operation(summary = "Create a new current account movement")
    @PreAuthorize("hasPermission('FINANCEIRO_CREATE')")
    public ResponseEntity<ApiResponse<ContaCorrente>> create(@Valid @RequestBody ContaCorrenteRequest request) {
        log.info("Creating new conta corrente movement");
        ContaCorrente conta = request.toEntity();
        ContaCorrente saved = contaCorrenteService.save(conta);
        return created(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get current account movement by ID")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<ContaCorrente>> findById(@PathVariable Long id) {
        ContaCorrente conta = contaCorrenteService.findByIdOrThrow(id);
        return success(conta);
    }

    @GetMapping
    @Operation(summary = "Get all current account movements with pagination and filtering")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<Page<ContaCorrente>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            ContaCorrenteFilter filter) {
        Page<ContaCorrente> contas = contaCorrenteService.filter(filter);
        return success(contas);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Get current account movements by client")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<ContaCorrente>>> findByCliente(@PathVariable Long clienteId) {
        // Add method to service
        return success(List.of());
    }

    @GetMapping("/fornecedor/{fornecedorId}")
    @Operation(summary = "Get current account movements by supplier")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<ContaCorrente>>> findByFornecedor(@PathVariable Long fornecedorId) {
        // Add method to service
        return success(List.of());
    }

    @GetMapping("/cliente/{clienteId}/debitos-nao-pagos")
    @Operation(summary = "Get unpaid debits by client")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<ContaCorrente>>> findDebitosNaoPagosByCliente(@PathVariable Long clienteId) {
        List<ContaCorrente> debitos = contaCorrenteService.findDebitosNaoPagosByCliente(clienteId);
        return success(debitos);
    }

    @GetMapping("/cliente/{clienteId}/total-debitos")
    @Operation(summary = "Get total unpaid debits by client")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> sumDebitosNaoPagosByCliente(@PathVariable Long clienteId) {
        BigDecimal total = contaCorrenteService.sumDebitosNaoPagosByCliente(clienteId);
        return success(total);
    }

    @GetMapping("/vencidas")
    @Operation(summary = "Get overdue current account movements")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<ContaCorrente>>> findVencidas() {
        List<ContaCorrente> contas = contaCorrenteService.findContasVencidas();
        return success(contas);
    }

    @PatchMapping("/{id}/pagar")
    @Operation(summary = "Mark current account movement as paid")
    @PreAuthorize("hasPermission('FINANCEIRO_UPDATE')")
    public ResponseEntity<ApiResponse<ContaCorrente>> marcarComoPago(@PathVariable Long id) {
        ContaCorrente conta = contaCorrenteService.marcarComoPago(id);
        return success(conta, "Conta corrente marked as paid");
    }

    @PostMapping("/cliente/{clienteId}/debito")
    @Operation(summary = "Create debit for client")
    @PreAuthorize("hasPermission('FINANCEIRO_CREATE')")
    public ResponseEntity<ApiResponse<ContaCorrente>> criarDebitoCliente(
            @PathVariable Long clienteId,
            @RequestParam Long documentoId,
            @RequestParam BigDecimal valor,
            @RequestParam String descricao,
            @RequestParam LocalDate dataVencimento) {
        ContaCorrente conta = contaCorrenteService.criarDebitoCliente(
                clienteId, documentoId, valor, descricao, dataVencimento);
        return created(conta);
    }

    @PostMapping("/cliente/{clienteId}/credito")
    @Operation(summary = "Create credit for client")
    @PreAuthorize("hasPermission('FINANCEIRO_CREATE')")
    public ResponseEntity<ApiResponse<ContaCorrente>> criarCreditoCliente(
            @PathVariable Long clienteId,
            @RequestParam Long documentoId,
            @RequestParam BigDecimal valor,
            @RequestParam String descricao) {
        ContaCorrente conta = contaCorrenteService.criarCreditoCliente(
                clienteId, documentoId, valor, descricao);
        return created(conta);
    }
}
