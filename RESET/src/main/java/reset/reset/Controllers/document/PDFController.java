package reset.reset.Controllers.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Controllers.base.BaseController;
import reset.reset.Services.document.PDFService;
import reset.reset.dto.document.pdf.PDFConfigDTO;
import reset.reset.dto.document.pdf.PDFResponse;

import java.util.Base64;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documento PDF", description = "PDF generation endpoints")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'GERENTE', 'CONTABILISTA')")
public class PDFController extends BaseController {

    private final PDFService pdfService;

    @PostMapping("/{id}/pdf")
    @Operation(summary = "Generate PDF for a document")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<ApiResponse<PDFResponse>> gerarPDF(
            @PathVariable Long id,
            @RequestBody(required = false) PDFConfigDTO config) {
        log.info("Gerando PDF para documento: {}", id);

        if (config == null) {
            config = PDFConfigDTO.builder()
                    .titulo("DOCUMENTO FISCAL")
                    .moeda("MZN")
                    .rodape("Documento gerado eletronicamente. Não necessita de assinatura.")
                    .build();
        }

        PDFResponse response = pdfService.gerarPDFOpenPDF(id, config);
        return success(response);
    }

    @GetMapping("/{id}/pdf/download")
    @Operation(summary = "Download PDF for a document")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<byte[]> downloadPDF(
            @PathVariable Long id,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String rodape) {
        log.info("Download PDF para documento: {}", id);

        PDFConfigDTO config = PDFConfigDTO.builder()
                .titulo(titulo != null ? titulo : "DOCUMENTO FISCAL")
                .rodape(rodape != null ? rodape : "Documento gerado eletronicamente.")
                .moeda("MZN")
                .build();

        return pdfService.downloadPDF(id, config);
    }

    @GetMapping("/{id}/pdf/view")
    @Operation(summary = "View PDF in browser")
    @PreAuthorize("hasPermission('DOCUMENTO_READ')")
    public ResponseEntity<byte[]> visualizarPDF(@PathVariable Long id) {
        log.info("Visualizando PDF para documento: {}", id);

        PDFConfigDTO config = PDFConfigDTO.builder()
                .titulo("DOCUMENTO FISCAL")
                .moeda("MZN")
                .build();

        return pdfService.visualizarPDF(id, config);
//        byte[] pdfBytes = Base64.getDecoder().decode(response.getFileBase64());

//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + response.getFileName() + "\"")
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(pdfBytes);
    }
}
