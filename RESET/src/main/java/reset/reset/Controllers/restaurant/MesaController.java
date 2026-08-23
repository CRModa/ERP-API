package reset.reset.Controllers.restaurant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Controllers.base.BaseController;
import reset.reset.Services.restaurant.MesaService;
import reset.reset.dto.request.restaurant.MesaRequest;
import reset.reset.dto.restaurant.MesaDTO;
import reset.reset.dto.restaurant.MesaResumoDTO;

import java.util.List;

@RestController
@RequestMapping("/restaurante/mesas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurante - Mesas", description = "Gerenciamento de mesas")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'GARCOM')")
public class MesaController extends BaseController {

    private final MesaService mesaService;

    @PostMapping
    @Operation(summary = "Criar uma nova mesa")
    @PreAuthorize("hasPermission('MESA_CREATE')")
    public ResponseEntity<ApiResponse<MesaDTO>> create(@Valid @RequestBody MesaRequest request) {
        log.info("Criando nova mesa: {}", request.getNumero());
        MesaDTO mesa = mesaService.criarMesa(request);
        return created(mesa);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma mesa existente")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<MesaDTO>> update(@PathVariable Long id,
                                                       @Valid @RequestBody MesaRequest request) {
        log.info("Atualizando mesa com id: {}", id);
        MesaDTO mesa = mesaService.atualizarMesa(id, request);
        return success(mesa, "Mesa atualizada com sucesso");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar mesa por ID")
    @PreAuthorize("hasPermission('MESA_READ')")
    public ResponseEntity<ApiResponse<MesaDTO>> findById(@PathVariable Long id) {
        MesaDTO mesa = mesaService.findMesaDTOById(id);
        return success(mesa);
    }

    @GetMapping
    @Operation(summary = "Listar mesas com paginação")
    @PreAuthorize("hasPermission('MESA_READ')")
    public ResponseEntity<ApiResponse<Page<MesaResumoDTO>>> findAll(
            @PageableDefault(size = 20, sort = "numero") Pageable pageable) {
        Page<MesaResumoDTO> mesas = mesaService.findMesasResumoByEmpresa(pageable);
        return success(mesas);
    }

    @GetMapping("/detalhes")
    @Operation(summary = "Listar mesas com detalhes e pedidos ativos")
    @PreAuthorize("hasPermission('MESA_READ')")
    public ResponseEntity<ApiResponse<Page<MesaDTO>>> findAllWithPedidos(
            @PageableDefault(size = 20, sort = "numero") Pageable pageable) {
        Page<MesaDTO> mesas = mesaService.findMesasWithPedidos(pageable);
        return success(mesas);
    }

    @GetMapping("/disponiveis")
    @Operation(summary = "Listar mesas disponíveis")
    @PreAuthorize("hasPermission('MESA_READ')")
    public ResponseEntity<ApiResponse<List<MesaDTO>>> findDisponiveis() {
        List<MesaDTO> mesas = mesaService.findMesasDisponiveisDTO();
        return success(mesas);
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas as mesas")
    @PreAuthorize("hasPermission('MESA_READ')")
    public ResponseEntity<ApiResponse<List<MesaDTO>>> findAllMesas() {
        List<MesaDTO> mesas = mesaService.findAllMesas();
        return success(mesas);
    }

    @PatchMapping("/{id}/ocupar")
    @Operation(summary = "Ocupar uma mesa")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<MesaDTO>> ocupar(@PathVariable Long id) {
        MesaDTO mesa = mesaService.ocuparMesa(id);
        return success(mesa, "Mesa ocupada com sucesso");
    }

    @PatchMapping("/{id}/liberar")
    @Operation(summary = "Liberar uma mesa")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<MesaDTO>> liberar(@PathVariable Long id) {
        MesaDTO mesa = mesaService.liberarMesa(id);
        return success(mesa, "Mesa liberada com sucesso");
    }

    @PatchMapping("/{id}/reservar")
    @Operation(summary = "Reservar uma mesa")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<MesaDTO>> reservar(@PathVariable Long id) {
        MesaDTO mesa = mesaService.reservarMesa(id);
        return success(mesa, "Mesa reservada com sucesso");
    }

    @PatchMapping("/{id}/limpeza")
    @Operation(summary = "Marcar mesa em limpeza")
    @PreAuthorize("hasPermission('MESA_UPDATE')")
    public ResponseEntity<ApiResponse<MesaDTO>> marcarLimpeza(@PathVariable Long id) {
        MesaDTO mesa = mesaService.marcarEmLimpeza(id);
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