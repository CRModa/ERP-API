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
import reset.reset.Models.restaurant.Pedido;
import reset.reset.Services.restaurant.PedidoService;
import reset.reset.dto.request.restaurant.ItemPedidoRequest;
import reset.reset.dto.request.restaurant.PedidoRequest;
import reset.reset.dto.restaurant.PedidoDTO;
import reset.reset.dto.restaurant.PedidoResumoDTO;

import java.util.List;

@RestController
@RequestMapping("/restaurante/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurante - Pedidos", description = "Gerenciamento de pedidos")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'GARCOM')")
public class PedidoController extends BaseController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Criar um novo pedido")
    @PreAuthorize("hasPermission('PEDIDO_CREATE')")
    public ResponseEntity<ApiResponse<PedidoDTO>> criar(@Valid @RequestBody PedidoRequest request) {
        log.info("Criando novo pedido - Mesa: {}, Itens: {}",
                request.getMesaId(),
                request.getItens() != null ? request.getItens().size() : 0);

        PedidoDTO pedido = pedidoService.criarPedido(request);
        return created(pedido);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    @PreAuthorize("hasPermission('PEDIDO_READ')")
    public ResponseEntity<ApiResponse<PedidoDTO>> findById(@PathVariable Long id) {
        PedidoDTO pedido = pedidoService.findPedidoDTOById(id);
        return success(pedido);
    }

    @GetMapping
    @Operation(summary = "Listar pedidos com paginação")
    @PreAuthorize("hasPermission('PEDIDO_READ')")
    public ResponseEntity<ApiResponse<Page<PedidoResumoDTO>>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PedidoResumoDTO> pedidos = pedidoService.findPedidosResumoByEmpresa(pageable);
        return success(pedidos);
    }

    @GetMapping("/mesa/{mesaId}")
    @Operation(summary = "Listar pedidos ativos por mesa")
    @PreAuthorize("hasPermission('PEDIDO_READ')")
    public ResponseEntity<ApiResponse<List<PedidoDTO>>> findPedidosAtivosByMesa(@PathVariable Long mesaId) {
        List<PedidoDTO> pedidos = pedidoService.findPedidosAtivosByMesaDTO(mesaId);
        return success(pedidos);
    }

    @GetMapping("/em-andamento")
    @Operation(summary = "Listar pedidos em andamento")
    @PreAuthorize("hasPermission('PEDIDO_READ')")
    public ResponseEntity<ApiResponse<List<PedidoDTO>>> findEmAndamento() {
        List<PedidoDTO> pedidos = pedidoService.findPedidosEmAndamento();
        return success(pedidos);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido")
    @PreAuthorize("hasPermission('PEDIDO_UPDATE')")
    public ResponseEntity<ApiResponse<PedidoDTO>> atualizarStatus(
            @PathVariable Long id,
            @RequestParam Pedido.StatusPedido status) {
        PedidoDTO pedido = pedidoService.atualizarStatus(id, status);
        return success(pedido, "Status atualizado para: " + status);
    }

    @PostMapping("/{id}/itens")
    @Operation(summary = "Adicionar item ao pedido")
    @PreAuthorize("hasPermission('PEDIDO_UPDATE')")
    public ResponseEntity<ApiResponse<PedidoDTO>> adicionarItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemPedidoRequest request) {
        log.info("Adicionando item ao pedido: {} - produtoId: {}", id, request.getProdutoId());
        PedidoDTO pedido = pedidoService.adicionarItem(id, request);
        return success(pedido, "Item adicionado com sucesso");
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    @Operation(summary = "Remover item do pedido")
    @PreAuthorize("hasPermission('PEDIDO_UPDATE')")
    public ResponseEntity<ApiResponse<PedidoDTO>> removerItem(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        PedidoDTO pedido = pedidoService.removerItem(id, itemId);
        return success(pedido, "Item removido com sucesso");
    }
}