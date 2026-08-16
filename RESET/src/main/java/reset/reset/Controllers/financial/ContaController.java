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
import reset.reset.Models.financial.Conta;
import reset.reset.Models.financial.MovimentoConta;
import reset.reset.Services.financial.ContaService;
import reset.reset.dto.request.ContaRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conta", description = "Account management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTABILISTA', 'GERENTE')")
public class ContaController extends BaseController {

    private final ContaService contaService;

    @PostMapping
    @Operation(summary = "Create a new account")
    @PreAuthorize("hasPermission('FINANCEIRO_CREATE')")
    public ResponseEntity<ApiResponse<Conta>> create(@Valid @RequestBody ContaRequest request) {
        log.info("Creating new conta");
        Conta conta = request.toEntity();
        Conta saved = contaService.save(conta);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing account")
    @PreAuthorize("hasPermission('FINANCEIRO_UPDATE')")
    public ResponseEntity<ApiResponse<Conta>> update(@PathVariable Long id,
                                                     @Valid @RequestBody ContaRequest request) {
        log.info("Updating conta with id: {}", id);
        Conta conta = request.toEntity();
        conta.setId(id);
        Conta updated = contaService.update(id, conta);
        return success(updated, "Conta updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<Conta>> findById(@PathVariable Long id) {
        Conta conta = contaService.findByIdOrThrow(id);
        return success(conta);
    }

    @GetMapping
    @Operation(summary = "Get all accounts with pagination")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<Page<Conta>>> findAll(
            @PageableDefault(size = 20, sort = "descricao", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Conta> contas = contaService.findAll(pageable);
        return success(contas);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get accounts by company")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<Page<Conta>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Conta> contas = contaService.findByEmpresaId(empresaId, pageable);
        return success(contas);
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Get accounts by type")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<List<Conta>>> findByTipo(@PathVariable String tipo) {
        List<Conta> contas = contaService.findByTipo(tipo);
        return success(contas);
    }

    @GetMapping("/{id}/saldo")
    @Operation(summary = "Get account balance")
    @PreAuthorize("hasPermission('FINANCEIRO_READ')")
    public ResponseEntity<ApiResponse<BigDecimal>> getSaldo(@PathVariable Long id) {
        BigDecimal saldo = contaService.getSaldoConta(id);
        return success(saldo);
    }

    @PostMapping("/{id}/movimento")
    @Operation(summary = "Register account movement")
    @PreAuthorize("hasPermission('FINANCEIRO_CREATE')")
    public ResponseEntity<ApiResponse<MovimentoConta>> registrarMovimento(
            @PathVariable Long id,
            @RequestParam String tipo,
            @RequestParam BigDecimal valor,
            @RequestParam(required = false) Long documentoId,
            @RequestParam(required = false) LocalDate data) {
        MovimentoConta movimento = contaService.registrarMovimento(id, tipo, valor, documentoId, data);
        return created(movimento);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account")
    @PreAuthorize("hasPermission('FINANCEIRO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        contaService.deleteById(id);
        return noContent();
    }
}
