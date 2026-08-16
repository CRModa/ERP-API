package reset.reset.Controllers.product;

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
import reset.reset.Models.product.Desconto;
import reset.reset.Services.product.DescontoService;
import reset.reset.dto.request.DescontoRequest;

import java.util.List;

@RestController
@RequestMapping("/descontos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Desconto", description = "Discount management endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class DescontoController extends BaseController {

    private final DescontoService descontoService;

    @PostMapping
    @Operation(summary = "Create a new discount")
    public ResponseEntity<ApiResponse<Desconto>> create(@Valid @RequestBody DescontoRequest request) {
        log.info("Creating new desconto: {}", request.getDescricao());
        Desconto desconto = request.toEntity();
        Desconto saved = descontoService.save(desconto);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing discount")
    public ResponseEntity<ApiResponse<Desconto>> update(@PathVariable Long id,
                                                        @Valid @RequestBody DescontoRequest request) {
        log.info("Updating desconto with id: {}", id);
        Desconto desconto = request.toEntity();
        desconto.setId(id);
        Desconto updated = descontoService.update(id, desconto);
        return success(updated, "Desconto updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get discount by ID")
    public ResponseEntity<ApiResponse<Desconto>> findById(@PathVariable Long id) {
        Desconto desconto = descontoService.findByIdOrThrow(id);
        return success(desconto);
    }

    @GetMapping
    @Operation(summary = "Get all discounts")
    public ResponseEntity<ApiResponse<Page<Desconto>>> findAll(
            @PageableDefault(size = 20, sort = "descricao", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Desconto> descontos = descontoService.findAll(pageable);
        return success(descontos);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get discounts by company")
    public ResponseEntity<ApiResponse<Page<Desconto>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Desconto> descontos = descontoService.findByEmpresaId(empresaId, pageable);
        return success(descontos);
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Get discounts by type")
    public ResponseEntity<ApiResponse<List<Desconto>>> findByTipo(@PathVariable String tipo) {
        List<Desconto> descontos = descontoService.findByTipo(tipo);
        return success(descontos);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete discount")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        descontoService.deleteById(id);
        return noContent();
    }
}
