package reset.reset.Controllers.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Controllers.base.BaseController;
import reset.reset.Services.document.ImpressaoService;
import reset.reset.dto.document.pdf.ImpressaoRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/impressao")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Impressão", description = "Endpoints para impressão de recibos")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE')")
public class ImpressaoController extends BaseController {

    private final ImpressaoService impressaoService;

    @PostMapping("/recibo")
    @Operation(summary = "Imprime recibo térmico")
    @PreAuthorize("hasPermission('IMPRESSAO_CREATE')")
    public ResponseEntity<ApiResponse<Void>> imprimirRecibo(@Valid @RequestBody ImpressaoRequest request) {
        log.info("Solicitação de impressão - Documento: {}, Pedido: {}",
                request.getDocumentoId(), request.getPedidoId());

        impressaoService.imprimirRecibo(request);
        return success(null);
    }

    @GetMapping("/impressoras")
    @Operation(summary = "Lista impressoras disponíveis")
    @PreAuthorize("hasPermission('IMPRESSAO_READ')")
    public ResponseEntity<ApiResponse<List<String>>> listarImpressoras() {
        List<String> impressoras = impressaoService.listarImpressoras();
        return success(impressoras);
    }

    @GetMapping("/status")
    @Operation(summary = "Verifica disponibilidade da impressora")
    @PreAuthorize("hasPermission('IMPRESSAO_READ')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verificarStatus() {
        boolean disponivel = impressaoService.isImpressoraDisponivel();

        Map<String, Object> response = new HashMap<>();
        response.put("disponivel", disponivel);
        response.put("mensagem", disponivel ?
                "Impressora térmica disponível" :
                "Nenhuma impressora térmica encontrada");

        return success(response);
    }
}

