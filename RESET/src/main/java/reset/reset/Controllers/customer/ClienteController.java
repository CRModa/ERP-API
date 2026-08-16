package reset.reset.Controllers.customer;

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
import reset.reset.Models.customer.Cliente;
import reset.reset.Services.customer.ClienteService;
import reset.reset.dto.filter.ClienteFilter;
import reset.reset.dto.projection.ClienteResumo;
import reset.reset.dto.request.ClienteRequest;

import java.math.BigDecimal;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cliente", description = "Customer management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class ClienteController extends BaseController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<ApiResponse<Cliente>> create(@Valid @RequestBody ClienteRequest request) {
        log.info("Creating new cliente: {}", request.getNome());
        Cliente cliente = request.toEntity();
        Cliente saved = clienteService.save(cliente);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<ApiResponse<Cliente>> update(@PathVariable Long id,
                                                       @Valid @RequestBody ClienteRequest request) {
        log.info("Updating cliente with id: {}", id);
        Cliente cliente = request.toEntity();
        cliente.setId(id);
        Cliente updated = clienteService.update(id, cliente);
        return success(updated, "Cliente updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<Cliente>> findById(@PathVariable Long id) {
        Cliente cliente = clienteService.findByIdOrThrow(id);
        return success(cliente);
    }

    @GetMapping
    @Operation(summary = "Get all customers with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<Cliente>>> findAll(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable,
            ClienteFilter filter) {
        Page<Cliente> clientes = clienteService.filter(filter);
        return success(clientes);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get customers by company")
    public ResponseEntity<ApiResponse<Page<Cliente>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Cliente> clientes = clienteService.findByEmpresaId(empresaId, pageable);
        return success(clientes);
    }

    @GetMapping("/empresa/{empresaId}/active")
    @Operation(summary = "Get active customers by company")
    public ResponseEntity<ApiResponse<Page<Cliente>>> findActiveByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Cliente> clientes = clienteService.findActiveByEmpresaId(empresaId, pageable);
        return success(clientes);
    }

    @GetMapping("/empresa/{empresaId}/resumo")
    @Operation(summary = "Get customer summary by company")
    public ResponseEntity<ApiResponse<Page<ClienteResumo>>> findResumoByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ClienteResumo> clientes = clienteService.findClienteResumoByEmpresaId(empresaId, pageable);
        return success(clientes);
    }

    @PatchMapping("/{id}/saldo")
    @Operation(summary = "Update customer balance")
    public ResponseEntity<ApiResponse<Cliente>> updateSaldo(@PathVariable Long id,
                                                            @RequestParam BigDecimal valor) {
        Cliente cliente = clienteService.atualizarSaldo(id, valor);
        return success(cliente, "Saldo updated successfully");
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate customer")
    public ResponseEntity<ApiResponse<Cliente>> activate(@PathVariable Long id) {
        Cliente cliente = clienteService.ativarCliente(id);
        return success(cliente, "Cliente activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate customer")
    public ResponseEntity<ApiResponse<Cliente>> deactivate(@PathVariable Long id) {
        Cliente cliente = clienteService.desativarCliente(id);
        return success(cliente, "Cliente deactivated successfully");
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Get customers by type")
    public ResponseEntity<ApiResponse<Page<Cliente>>> findByTipo(
            @PathVariable String tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        // Add method to service
        return success(Page.empty());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        clienteService.deleteById(id);
        return noContent();
    }
}
