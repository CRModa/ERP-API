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
import reset.reset.Models.restaurant.ItemPedido;
import reset.reset.Models.restaurant.Pedido;
import reset.reset.Services.restaurant.PedidoService;
import reset.reset.dto.request.restaurant.AdicionarItemPedidoRequest;
import reset.reset.dto.request.restaurant.PedidoRequest;
import reset.reset.dto.response.PedidoEstatisticas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/restaurante/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurante - Pedidos", description = "Gerenciamento de pedidos do restaurante")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'GARCOM')")
public class PedidoController extends BaseController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Criar um novo pedido")
    @PreAuthorize("hasPermission('PEDIDO_CREATE')")
    public ResponseEntity<ApiResponse<Pedido>> criar(@Valid @RequestBody PedidoRequest request) {
        log.info("Criando novo pedido para mesa: {}", request.getMesaId());
        Pedido pedido = request.toEntity();
        Pedido saved = pedidoService.save(pedido);
        return created(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    @PreAuthorize("hasPermission('PEDIDO_READ')")
    public ResponseEntity<ApiResponse<Pedido>> findById(@PathVariable Long id) {
        Pedido pedido = pedidoService.findByIdOrThrow(id);
        return success(pedido);
    }

    @GetMapping
    @Operation(summary = "Listar pedidos com paginação")
    @PreAuthorize("hasPermission('PEDIDO_READ')")
    public ResponseEntity<ApiResponse<Page<Pedido>>> findAll(
            @RequestParam Long empresaId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Pedido> pedidos = pedidoService.findByEmpresaId(empresaId, pageable);
        return success(pedidos);
    }

    @GetMapping("/em-andamento")
    @Operation(summary = "Listar pedidos em andamento")
    @PreAuthorize("hasPermission('PEDIDO_READ')")
    public ResponseEntity<ApiResponse<List<Pedido>>> findEmAndamento(@RequestParam Long empresaId) {
        List<Pedido> pedidos = pedidoService.findPedidosEmAndamento(empresaId);
        return success(pedidos);
    }

    @GetMapping("/mesa/{mesaId}")
    @Operation(summary = "Listar pedidos ativos por mesa")
    @PreAuthorize("hasPermission('PEDIDO_READ')")
    public ResponseEntity<ApiResponse<List<Pedido>>> findPedidosAtivosByMesa(@PathVariable Long mesaId) {
        List<Pedido> pedidos = pedidoService.findPedidosAtivosByMesa(mesaId);
        return success(pedidos);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido")
    @PreAuthorize("hasPermission('PEDIDO_UPDATE')")
    public ResponseEntity<ApiResponse<Pedido>> atualizarStatus(
            @PathVariable Long id,
            @RequestParam Pedido.StatusPedido status) {
        Pedido pedido = pedidoService.atualizarStatus(id, status);
        return success(pedido, "Status atualizado para: " + status);
    }

    @PostMapping("/{id}/itens")
    @Operation(summary = "Adicionar item ao pedido")
    @PreAuthorize("hasPermission('PEDIDO_UPDATE')")
    public ResponseEntity<ApiResponse<Pedido>> adicionarItem(
            @PathVariable Long id,
            @Valid @RequestBody AdicionarItemPedidoRequest request) {
        log.info("Adicionando item ao pedido: {}", id);
        ItemPedido item = request.toEntity();
        Pedido pedido = pedidoService.adicionarItem(id, item);
        return success(pedido, "Item adicionado com sucesso");
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @Operation(summary = "Remover item do pedido")
    @PreAuthorize("hasPermission('PEDIDO_UPDATE')")
    public ResponseEntity<ApiResponse<Pedido>> removerItem(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        Pedido pedido = pedidoService.removerItem(id, itemId);
        return success(pedido, "Item removido com sucesso");
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatísticas de pedidos")
    @PreAuthorize("hasPermission('PEDIDO_READ')")
    public ResponseEntity<ApiResponse<PedidoEstatisticas>> getEstatisticas(
            @RequestParam Long empresaId,
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim) {

        Long totalPedidos = pedidoService.countByEmpresaAndPeriodo(empresaId, inicio, fim);
        BigDecimal totalVendas = pedidoService.sumTotalByEmpresaAndPeriodo(empresaId, inicio, fim);

        PedidoEstatisticas stats = PedidoEstatisticas.builder()
                .totalPedidos(totalPedidos)
                .totalVendas(totalVendas)
                .build();

        return success(stats);
    }
}
