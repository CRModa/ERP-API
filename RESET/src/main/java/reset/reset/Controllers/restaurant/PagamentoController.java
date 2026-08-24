package reset.reset.Controllers.restaurant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Controllers.base.BaseController;
import reset.reset.Services.restaurant.PagamentoService;
import reset.reset.dto.restaurant.PagamentoEstatisticasDTO;
import reset.reset.dto.restaurant.PagamentoRequest;
import reset.reset.dto.restaurant.PagamentoResponse;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/restaurante/pagamentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurante - Pagamentos", description = "Gerenciamento de pagamentos")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'GARCOM')")
public class PagamentoController extends BaseController {

    private final PagamentoService pagamentoService;

    @PostMapping
    @Operation(summary = "Processar pagamento de um pedido")
    @PreAuthorize("hasPermission('PAGAMENTO_CREATE')")
    public ResponseEntity<ApiResponse<PagamentoResponse>> processarPagamento(
            @Valid @RequestBody PagamentoRequest request) {
        log.info("Processando pagamento - Pedido: {}, Método: {}", request.getPedidoId(), request.getMetodo());
        PagamentoResponse response = pagamentoService.processarPagamento(request);
        return created(response);
    }

    @PostMapping("/multiples")
    @Operation(summary = "Processar pagamento de múltiplos pedidos")
    @PreAuthorize("hasPermission('PAGAMENTO_CREATE')")
    public ResponseEntity<ApiResponse<List<PagamentoResponse>>> processarPagamentosMultiplos(
            @Valid @RequestBody List<PagamentoRequest> requests) {
        log.info("Processando {} pagamentos", requests.size());
        List<PagamentoResponse> responses = pagamentoService.processarPagamentosMultiplos(requests);
        return created(responses);
    }

    @GetMapping
    @Operation(summary = "Listar pagamentos por período")
    @PreAuthorize("hasPermission('PAGAMENTO_READ')")
    public ResponseEntity<ApiResponse<List<PagamentoResponse>>> listarPagamentos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<PagamentoResponse> pagamentos = pagamentoService.findPagamentosByPeriodo(inicio, fim);
        return success(pagamentos);
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatísticas de pagamentos")
    @PreAuthorize("hasPermission('PAGAMENTO_READ')")
    public ResponseEntity<ApiResponse<PagamentoEstatisticasDTO>> getEstatisticas() {
        PagamentoEstatisticasDTO estatisticas = pagamentoService.getEstatisticasPagamentos();
        return success(estatisticas);
    }

    @PatchMapping("/{documentoId}/cancelar")
    @Operation(summary = "Cancelar um pagamento")
    @PreAuthorize("hasPermission('PAGAMENTO_DELETE')")
    public ResponseEntity<ApiResponse<Void>> cancelarPagamento(
            @PathVariable Long documentoId,
            @RequestParam String motivo) {
        log.info("Cancelando pagamento - Documento: {}, Motivo: {}", documentoId, motivo);
        pagamentoService.cancelarPagamento(documentoId, motivo);
        return success(null, "Pagamento cancelado com sucesso");
    }
}
