package reset.reset.Controllers.restaurant;

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
import reset.reset.Models.restaurant.Mesa;
import reset.reset.Services.restaurant.MesaService;
import reset.reset.dto.request.restaurant.MesaRequest;
import reset.reset.dto.response.MesaEstatisticas;

import java.util.List;

@RestController
@RequestMapping("/restaurante/mesas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurante - Mesas", description = "Gerenciamento de mesas do restaurante")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'GARCOM')")
public class MesaController extends BaseController {

    private final MesaService mesaService;

    @PostMapping
    @Operation(summary = "Criar uma nova mesa")
    @PreAuthorize("hasPermission('MESA_CREATE')")
    public ResponseEntity<ApiResponse<Mesa>> create(@Valid @RequestBody MesaRequest request) {
        log.info("Criando nova mesa: {}", request.getNumero());
        Mesa mesa = request.toEntity();
        Mesa saved = mesaService.save(mesa);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma mesa existente")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<Mesa>> update(@PathVariable Long id,
                                                    @Valid @RequestBody MesaRequest request) {
        log.info("Atualizando mesa com id: {}", id);
        Mesa mesa = request.toEntity();
        mesa.setId(id);
        Mesa updated = mesaService.update(id, mesa);
        return success(updated, "Mesa atualizada com sucesso");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar mesa por ID")
    @PreAuthorize("hasPermission('MESA_READ')")
    public ResponseEntity<ApiResponse<Mesa>> findById(@PathVariable Long id) {
        Mesa mesa = mesaService.findByIdOrThrow(id);
        return success(mesa);
    }

    @GetMapping
    @Operation(summary = "Listar todas as mesas com paginação")
    @PreAuthorize("hasPermission('MESA_READ')")
    public ResponseEntity<ApiResponse<Page<Mesa>>> findAll(
            @PageableDefault(size = 20, sort = "numero", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam Long empresaId) {
        Page<Mesa> mesas = mesaService.findActiveByEmpresaId(empresaId, pageable);
        return success(mesas);
    }

    @GetMapping("/disponiveis")
    @Operation(summary = "Listar mesas disponíveis")
    @PreAuthorize("hasPermission('MESA_READ')")
    public ResponseEntity<ApiResponse<List<Mesa>>> findDisponiveis(@RequestParam Long empresaId) {
        List<Mesa> mesas = mesaService.findMesasDisponiveis(empresaId);
        return success(mesas);
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatísticas das mesas")
    @PreAuthorize("hasPermission('MESA_READ')")
    public ResponseEntity<ApiResponse<MesaEstatisticas>> getEstatisticas(@RequestParam Long empresaId) {
        Long total = mesaService.countActiveByEmpresaId(empresaId);
        Long ocupadas = mesaService.countMesasOcupadas(empresaId);

        MesaEstatisticas stats = MesaEstatisticas.builder()
                .total(total)
                .ocupadas(ocupadas)
                .disponiveis(total - ocupadas)
                .build();

        return success(stats);
    }

    @PatchMapping("/{id}/ocupar")
    @Operation(summary = "Ocupar uma mesa")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<Mesa>> ocupar(@PathVariable Long id) {
        Mesa mesa = mesaService.ocuparMesa(id);
        return success(mesa, "Mesa ocupada com sucesso");
    }

    @PatchMapping("/{id}/liberar")
    @Operation(summary = "Liberar uma mesa")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<Mesa>> liberar(@PathVariable Long id) {
        Mesa mesa = mesaService.liberarMesa(id);
        return success(mesa, "Mesa liberada com sucesso");
    }

    @PatchMapping("/{id}/reservar")
    @Operation(summary = "Reservar uma mesa")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<Mesa>> reservar(@PathVariable Long id) {
        Mesa mesa = mesaService.reservarMesa(id);
        return success(mesa, "Mesa reservada com sucesso");
    }

    @PatchMapping("/{id}/limpeza")
    @Operation(summary = "Marcar mesa em limpeza")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<Mesa>> marcarLimpeza(@PathVariable Long id) {
        Mesa mesa = mesaService.marcarEmLimpeza(id);
        return success(mesa, "Mesa marcada em limpeza");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir mesa")
    @PreAuthorize("hasPermission('MESA_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        mesaService.deleteById(id);
        return noContent();
    }
}