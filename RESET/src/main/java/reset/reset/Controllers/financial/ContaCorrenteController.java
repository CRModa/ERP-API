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
import reset.reset.dto.financial.ContaCorrenteDTO;
import reset.reset.dto.financial.ContaCorrenteResumoDTO;
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
    public ResponseEntity<ApiResponse<ContaCorrenteDTO>> create(@Valid @RequestBody ContaCorrenteRequest request) {
        log.info("Creating new conta corrente movement");
        ContaCorrente conta = request.toEntity();
        ContaCorrente saved = contaCorrenteService.save(conta);
        return created(ContaCorrenteDTO.fromEntity(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get current account movement by ID")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<ContaCorrenteDTO>> findById(@PathVariable Long id) {
        ContaCorrenteDTO conta = contaCorrenteService.findByIdDTO(id);
        return success(conta);
    }

    @GetMapping
    @Operation(summary = "Get all current account movements with pagination and filtering")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<Page<ContaCorrenteDTO>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            ContaCorrenteFilter filter) {
        Page<ContaCorrenteDTO> contas = contaCorrenteService.filterDTO(filter);
        return success(contas);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Get current account movements by client")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<ContaCorrenteDTO>>> findByCliente(@PathVariable Long clienteId) {
        List<ContaCorrenteDTO> contas = contaCorrenteService.findByClienteIdDTO(clienteId);
        return success(contas);
    }

    @GetMapping("/cliente/{clienteId}/paginado")
    @Operation(summary = "Get current account movements by client with pagination")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<Page<ContaCorrenteDTO>>> findByClientePaginado(
            @PathVariable Long clienteId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ContaCorrenteDTO> contas = contaCorrenteService.findByClienteIdDTO(clienteId, pageable);
        return success(contas);
    }

    @GetMapping("/fornecedor/{fornecedorId}")
    @Operation(summary = "Get current account movements by supplier")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<ContaCorrenteDTO>>> findByFornecedor(@PathVariable Long fornecedorId) {
        List<ContaCorrenteDTO> contas = contaCorrenteService.findByFornecedorIdDTO(fornecedorId);
        return success(contas);
    }

    @GetMapping("/fornecedor/{fornecedorId}/paginado")
    @Operation(summary = "Get current account movements by supplier with pagination")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<Page<ContaCorrenteDTO>>> findByFornecedorPaginado(
            @PathVariable Long fornecedorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ContaCorrenteDTO> contas = contaCorrenteService.findByFornecedorIdDTO(fornecedorId, pageable);
        return success(contas);
    }

    @GetMapping("/cliente/{clienteId}/debitos-nao-pagos")
    @Operation(summary = "Get unpaid debits by client")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<ContaCorrenteResumoDTO>>> findDebitosNaoPagosByCliente(@PathVariable Long clienteId) {
        List<ContaCorrenteResumoDTO> debitos = contaCorrenteService.findDebitosNaoPagosByClienteDTO(clienteId);
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
    public ResponseEntity<ApiResponse<List<ContaCorrenteDTO>>> findVencidas() {
        List<ContaCorrenteDTO> contas = contaCorrenteService.findContasVencidasDTO();
        return success(contas);
    }

    @PatchMapping("/{id}/pagar")
    @Operation(summary = "Mark current account movement as paid")
    @PreAuthorize("hasPermission('FINANCEIRO_UPDATE')")
    public ResponseEntity<ApiResponse<ContaCorrenteDTO>> marcarComoPago(@PathVariable Long id) {
        ContaCorrenteDTO conta = contaCorrenteService.marcarComoPagoDTO(id);
        return success(conta, "Conta corrente marked as paid");
    }

    @PostMapping("/cliente/{clienteId}/debito")
    @Operation(summary = "Create debit for client")
    @PreAuthorize("hasPermission('FINANCEIRO_CREATE')")
    public ResponseEntity<ApiResponse<ContaCorrenteDTO>> criarDebitoCliente(
            @PathVariable Long clienteId,
            @RequestParam Long documentoId,
            @RequestParam BigDecimal valor,
            @RequestParam String descricao,
            @RequestParam LocalDate dataVencimento) {
        ContaCorrenteDTO conta = contaCorrenteService.criarDebitoClienteDTO(
                clienteId, documentoId, valor, descricao, dataVencimento);
        return created(conta);
    }

    @PostMapping("/cliente/{clienteId}/credito")
    @Operation(summary = "Create credit for client")
    @PreAuthorize("hasPermission('FINANCEIRO_CREATE')")
    public ResponseEntity<ApiResponse<ContaCorrenteDTO>> criarCreditoCliente(
            @PathVariable Long clienteId,
            @RequestParam Long documentoId,
            @RequestParam BigDecimal valor,
            @RequestParam String descricao) {
        ContaCorrenteDTO conta = contaCorrenteService.criarCreditoClienteDTO(
                clienteId, documentoId, valor, descricao);
        return created(conta);
    }

    @GetMapping("/cliente/{clienteId}/saldo-atual")
    @Operation(summary = "Get current balance for client")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getSaldoAtualCliente(@PathVariable Long clienteId) {
        BigDecimal saldo = contaCorrenteService.getSaldoAtualCliente(clienteId);
        return success(saldo);
    }

    @GetMapping("/fornecedor/{fornecedorId}/saldo-atual")
    @Operation(summary = "Get current balance for supplier")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getSaldoAtualFornecedor(@PathVariable Long fornecedorId) {
        BigDecimal saldo = contaCorrenteService.getSaldoAtualFornecedor(fornecedorId);
        return success(saldo);
    }
}