package reset.reset.Services.document;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.document.Documento;
import reset.reset.Models.document.DocumentoItem;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.dto.document.pdf.PDFConfigDTO;
import reset.reset.dto.document.pdf.PDFResponse;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Currency;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFService {

    private final DocumentoRepository documentoRepository;
    private final EmpresaRepository empresaRepository;
    private final TemplateEngine templateEngine;

    // ==================== CONSTANTES ====================

    private static final String FONT_PATH = "fonts/DejaVuSans.ttf";
    private static final String FONT_BOLD_PATH = "fonts/DejaVuSans-Bold.ttf";

    // Paleta de cores
    private static final DeviceRgb PRIMARY      = new DeviceRgb(30, 41, 59);      // Slate-800
    private static final DeviceRgb SECONDARY    = new DeviceRgb(100, 116, 139);   // Slate-500
    private static final DeviceRgb ACCENT       = new DeviceRgb(37, 99, 235);     // Blue-600
    private static final DeviceRgb LIGHT_BG     = new DeviceRgb(248, 250, 252);  // Slate-50
    private static final DeviceRgb ALT_ROW      = new DeviceRgb(241, 245, 249);  // Slate-100
    private static final DeviceRgb TOTAL_BG      = new DeviceRgb(226, 232, 240);  // Slate-200
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(203, 213, 225);  // Slate-300
    private static final DeviceRgb WHITE        = new DeviceRgb(255, 255, 255);

    private static final float MARGIN = 42f;

    // ==================== MÉTODO PRINCIPAL ====================

    @Transactional(readOnly = true)
    public PDFResponse gerarPDFItext(Long documentoId, PDFConfigDTO config) {
        try {
            Documento documento = documentoRepository.findById(documentoId)
                    .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado: " + documentoId));

            PdfFont fontNormal = createFont(FONT_PATH);
            PdfFont fontBold = createFont(FONT_BOLD_PATH);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);

            // Rodapé em todas as páginas
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new PageFooterHandler(config, fontNormal));

            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(MARGIN, MARGIN, 70, MARGIN);

            buildHeader(document, config, fontNormal, fontBold);
            buildTitleAndBadge(document, config, documento, fontBold);
            buildDocumentInfo(document, documento, fontNormal, fontBold);
            buildItemsTable(document, documento, fontNormal, fontBold);
            buildTotals(document, documento, fontNormal, fontBold);
            buildObservations(document, documento, fontNormal, fontBold);

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return PDFResponse.builder()
                    .fileName("documento_" + safe(documento.getNumero(), String.valueOf(documentoId)) + ".pdf")
                    .fileBase64(Base64.getEncoder().encodeToString(pdfBytes))
                    .contentType("application/pdf")
                    .fileSize((long) pdfBytes.length)
                    .downloadUrl("/api/documentos/" + documentoId + "/pdf/download")
                    .build();

        } catch (Exception e) {
            log.error("Erro ao gerar PDF: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    // ==================== SECÇÕES DO PDF ====================

    private void buildHeader(Document doc, PDFConfigDTO config, PdfFont normal, PdfFont bold) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1.3f, 2.7f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(12);

        // Logo
        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingRight(12);

        if (hasText(config.getLogoBase64())) {
            try {
                byte[] bytes = Base64.getDecoder().decode(config.getLogoBase64());
                Image logo = new Image(com.itextpdf.io.image.ImageDataFactory.create(bytes));
                logo.scaleToFit(105, 65);
                logoCell.add(logo);
            } catch (Exception e) {
                log.warn("Falha ao carregar logo: {}", e.getMessage());
            }
        }
        header.addCell(logoCell);

        // Dados da empresa
        Cell infoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        if (hasText(config.getEmpresaNome())) {
            infoCell.add(new Paragraph(config.getEmpresaNome())
                    .setFont(bold)
                    .setFontSize(14)
                    .setFontColor(PRIMARY)
                    .setMarginBottom(3));
        }

        addMutedLine(infoCell, "NUIT: " + safe(config.getEmpresaNuit()), normal);
        addMutedLine(infoCell, safe(config.getEmpresaEndereco()), normal);
        addMutedLine(infoCell, join("Tel: ", config.getEmpresaTelefone()), normal);
        addMutedLine(infoCell, join("Email: ", config.getEmpresaEmail()), normal);

        header.addCell(infoCell);
        doc.add(header);

        // Barra de destaque
        doc.add(new Div()
                .setHeight(2.5f)
                .setBackgroundColor(ACCENT)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(18));
    }

    private void buildTitleAndBadge(Document doc, PDFConfigDTO config, Documento documento, PdfFont bold) {
        String titulo = hasText(config.getTitulo())
                ? config.getTitulo().toUpperCase()
                : "DOCUMENTO FISCAL";

        Table titleTable = new Table(UnitValue.createPercentArray(new float[]{2.5f, 1.5f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        Cell titleCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph(titulo)
                        .setFont(bold)
                        .setFontSize(15)
                        .setFontColor(PRIMARY));

        Cell badgeCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        String numero = safe(documento.getNumero(), "—");

        Div badge = new Div()
                .setBackgroundColor(LIGHT_BG)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.8f))
                .setPadding(6)
                .setPaddingLeft(12)
                .setPaddingRight(12);

        badge.add(new Paragraph("Nº " + numero)
                .setFont(bold)
                .setFontSize(11)
                .setFontColor(PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMargin(0));

        badgeCell.add(badge);
        titleTable.addCell(titleCell);
        titleTable.addCell(badgeCell);
        doc.add(titleTable);
    }

    private void buildDocumentInfo(Document doc, Documento documento, PdfFont normal, PdfFont bold) {
        Table info = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20)
                .setBackgroundColor(LIGHT_BG)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.6f))
                .setPadding(10);

        String data = documento.getData() != null
                ? documento.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "—";

        String cliente = documento.getCliente() != null
                ? safe(documento.getCliente().getNome(), "Cliente não informado")
                : "Cliente não informado";

        String nuitCliente = (documento.getCliente() != null && hasText(documento.getCliente().getNuit()))
                ? documento.getCliente().getNuit()
                : "—";

        info.addCell(createInfoBlock("Cliente", cliente, normal, bold, TextAlignment.LEFT));
        info.addCell(createInfoBlock("Data de Emissão", data, normal, bold, TextAlignment.RIGHT));
        info.addCell(createInfoBlock("NUIT do Cliente", nuitCliente, normal, bold, TextAlignment.LEFT));
        info.addCell(createInfoBlock("Nº Documento", safe(documento.getNumero(), "—"), normal, bold, TextAlignment.RIGHT));

        doc.add(info);
    }

    private void buildItemsTable(Document doc, Documento documento, PdfFont normal, PdfFont bold) {
        doc.add(new Paragraph("Detalhe dos Itens")
                .setFont(bold)
                .setFontSize(10.5f)
                .setFontColor(PRIMARY)
                .setMarginBottom(8));

        Table table = new Table(UnitValue.createPercentArray(new float[]{34, 9, 17, 15, 15}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(18);

        // Cabeçalho
        String[] headers = {"Descrição", "Qtd", "Preço Unit.", "Desconto", "Subtotal"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h)
                            .setFont(bold)
                            .setFontSize(8.5f)
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        NumberFormat currency = currencyFormat();
        boolean alternate = false;

        if (documento.getItens() == null || documento.getItens().isEmpty()) {
            Cell empty = new Cell(1, 5)
                    .add(new Paragraph("Nenhum item registado.")
                            .setFont(normal)
                            .setFontSize(9)
                            .setFontColor(SECONDARY)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setPadding(12)
                    .setBorder(new SolidBorder(BORDER_COLOR, 0.5f));
            table.addCell(empty);
        } else {
            for (DocumentoItem item : documento.getItens()) {
                DeviceRgb bg = alternate ? ALT_ROW : WHITE;
                alternate = !alternate;

                String desc = item.getProduto() != null
                        ? safe(item.getProduto().getNome(), "Produto não informado")
                        : "Produto não informado";

                String qtd = item.getQuantidade() != null
                        ? item.getQuantidade().toString()
                        : "0";

                String preco = item.getPrecoUnitario() != null
                        ? currency.format(item.getPrecoUnitario())
                        : "0,00";

                String desconto = "—";
                if (item.getDesconto() != null
                        && item.getDesconto().getValor() != null
                        && item.getDesconto().getValor().compareTo(BigDecimal.ZERO) > 0) {
                    desconto = currency.format(item.getDesconto().getValor());
                }

                BigDecimal subtotal = BigDecimal.ZERO;
                if (item.getPrecoUnitario() != null && item.getQuantidade() != null) {
                    subtotal = item.getPrecoUnitario().multiply(item.getQuantidade());
                    if (item.getDesconto() != null && item.getDesconto().getValor() != null) {
                        subtotal = subtotal.subtract(item.getDesconto().getValor());
                    }
                }

                table.addCell(itemCell(desc, normal, TextAlignment.LEFT, bg));
                table.addCell(itemCell(qtd, normal, TextAlignment.CENTER, bg));
                table.addCell(itemCell(preco, normal, TextAlignment.RIGHT, bg));
                table.addCell(itemCell(desconto, normal, TextAlignment.RIGHT, bg));
                table.addCell(itemCell(currency.format(subtotal), bold, TextAlignment.RIGHT, bg));
            }
        }

        doc.add(table);
    }

    private void buildTotals(Document doc, Documento documento, PdfFont normal, PdfFont bold) {
        NumberFormat currency = currencyFormat();
        BigDecimal total = documento.getTotal() != null ? documento.getTotal() : BigDecimal.ZERO;

        Table totals = new Table(UnitValue.createPercentArray(new float[]{1.4f, 1}))
                .setWidth(UnitValue.createPercentValue(42))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setMarginBottom(22);

        // Subtotal
        totals.addCell(totalLabel("Subtotal", normal, false));
        totals.addCell(totalValue(currency.format(total), normal, false));

        // Desconto do cliente
        if (documento.getCliente() != null
                && documento.getCliente().getDescontoPadrao() != null
                && documento.getCliente().getDescontoPadrao().compareTo(BigDecimal.ZERO) > 0) {

            totals.addCell(totalLabel("Desconto", normal, false));
            totals.addCell(totalValue(
                    "- " + currency.format(documento.getCliente().getDescontoPadrao()),
                    normal, false));
        }

        // TOTAL
        totals.addCell(totalLabel("TOTAL", bold, true));
        totals.addCell(totalValue(currency.format(total), bold, true));

        doc.add(totals);
    }

    private void buildObservations(Document doc, Documento documento, PdfFont normal, PdfFont bold) {
        if (!hasText(documento.getObservacao())) {
            return;
        }

        doc.add(new Paragraph("Observações")
                .setFont(bold)
                .setFontSize(9.5f)
                .setFontColor(PRIMARY)
                .setMarginBottom(4));

        Div box = new Div()
                .setBackgroundColor(LIGHT_BG)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.6f))
                .setPadding(10)
                .setMarginBottom(10);

        box.add(new Paragraph(documento.getObservacao())
                .setFont(normal)
                .setFontSize(9)
                .setFontColor(SECONDARY)
                .setMargin(0));

        doc.add(box);
    }

    // ==================== HANDLER DE RODAPÉ ====================

    private static class PageFooterHandler implements IEventHandler {

        private final PDFConfigDTO config;
        private final PdfFont font;

        PageFooterHandler(PDFConfigDTO config, PdfFont font) {
            this.config = config;
            this.font = font;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            int pageNumber = pdf.getPageNumber(page);
            int totalPages = pdf.getNumberOfPages();

            PdfCanvas pdfCanvas = new PdfCanvas(
                    page.newContentStreamBefore(),
                    page.getResources(),
                    pdf);

            Canvas canvas = new Canvas(pdfCanvas, pageSize);

            float y = 32;

            // Linha superior do rodapé
            pdfCanvas.setStrokeColor(BORDER_COLOR)
                    .setLineWidth(0.6f)
                    .moveTo(MARGIN, y + 18)
                    .lineTo(pageSize.getWidth() - MARGIN, y + 18)
                    .stroke();

            // Texto do rodapé (esquerda)
            String rodape = hasText(config.getRodape()) ? config.getRodape() : "";
            canvas.showTextAligned(
                    new Paragraph(rodape)
                            .setFont(font)
                            .setFontSize(7.5f)
                            .setFontColor(SECONDARY),
                    MARGIN, y + 6, TextAlignment.LEFT);

            // Data de geração (centro)
            String gerado = "Gerado em " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            canvas.showTextAligned(
                    new Paragraph(gerado)
                            .setFont(font)
                            .setFontSize(7.5f)
                            .setFontColor(SECONDARY),
                    pageSize.getWidth() / 2, y + 6, TextAlignment.CENTER);

            // Número da página (direita)
            canvas.showTextAligned(
                    new Paragraph("Página " + pageNumber + " de " + totalPages)
                            .setFont(font)
                            .setFontSize(7.5f)
                            .setFontColor(SECONDARY),
                    pageSize.getWidth() - MARGIN, y + 6, TextAlignment.RIGHT);

            canvas.close();
        }
    }

    // ==================== HELPERS DE UI ====================

    private Cell createInfoBlock(String label, String value, PdfFont normal, PdfFont bold, TextAlignment align) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(4)
                .setTextAlignment(align);

        cell.add(new Paragraph(label)
                .setFont(normal)
                .setFontSize(7.5f)
                .setFontColor(SECONDARY)
                .setMarginBottom(1));

        cell.add(new Paragraph(value)
                .setFont(bold)
                .setFontSize(10)
                .setFontColor(PRIMARY)
                .setMargin(0));

        return cell;
    }

    private Cell itemCell(String text, PdfFont font, TextAlignment align, DeviceRgb bg) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(8.5f)
                        .setFontColor(PRIMARY))
                .setBackgroundColor(bg)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.4f))
                .setPadding(7)
                .setTextAlignment(align)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell totalLabel(String text, PdfFont font, boolean isTotal) {
        Cell cell = new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(isTotal ? 10.5f : 9)
                        .setFontColor(isTotal ? PRIMARY : SECONDARY))
                .setBorder(Border.NO_BORDER)
                .setPadding(6)
                .setTextAlignment(TextAlignment.RIGHT);

        if (isTotal) {
            cell.setBackgroundColor(TOTAL_BG)
                    .setBorderTop(new SolidBorder(PRIMARY, 1.2f));
        }
        return cell;
    }

    private Cell totalValue(String text, PdfFont font, boolean isTotal) {
        Cell cell = new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(isTotal ? 11 : 9)
                        .setFontColor(PRIMARY))
                .setBorder(Border.NO_BORDER)
                .setPadding(6)
                .setTextAlignment(TextAlignment.RIGHT);

        if (isTotal) {
            cell.setBackgroundColor(TOTAL_BG)
                    .setBorderTop(new SolidBorder(PRIMARY, 1.2f));
        }
        return cell;
    }

    private void addMutedLine(Cell cell, String text, PdfFont font) {
        if (!hasText(text)) {
            return;
        }
        cell.add(new Paragraph(text)
                .setFont(font)
                .setFontSize(8.5f)
                .setFontColor(SECONDARY)
                .setMarginBottom(1.5f));
    }

    // ==================== UTILITÁRIOS ====================

    private PdfFont createFont(String path) throws Exception {
        return PdfFontFactory.createFont(
                new ClassPathResource(path).getPath(),
                "Identity-H",
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
        );
    }

    private NumberFormat currencyFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "MZ"));
        symbols.setCurrencySymbol("MZN");
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat format = new DecimalFormat("#,##0.00 ¤", symbols);
        format.setCurrency(Currency.getInstance("MZN"));
        return format;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static String safe(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private static String join(String prefix, String value) {
        return hasText(value) ? prefix + value : "";
    }

    // ==================== OUTROS MÉTODOS ====================

    @Transactional(readOnly = true)
    public byte[] gerarPDFHtml(Long documentoId) {
        try {
            Documento documento = documentoRepository.findById(documentoId)
                    .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

            Context context = new Context();
            context.setVariable("documento", documento);
            context.setVariable("cliente", documento.getCliente());
            context.setVariable("empresa", documento.getEmpresa());
            context.setVariable("itens", documento.getItens());
            context.setVariable("dataFormatada",
                    documento.getData() != null
                            ? documento.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : "—");
            context.setVariable("formatCurrency", currencyFormat());

            String html = templateEngine.process("documento-pdf", context);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);

            ConverterProperties props = new ConverterProperties();
            props.setBaseUri("classpath:/templates/");

            HtmlConverter.convertToPdf(html, pdfDoc, props);
            pdfDoc.close();

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erro ao gerar PDF HTML: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadPDF(Long documentoId, PDFConfigDTO config) {
        PDFResponse response = gerarPDFItext(documentoId, config);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + response.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(Base64.getDecoder().decode(response.getFileBase64()));
    }

    @Transactional(readOnly = true)
    public byte[] gerarPDFMultiplos(java.util.List<Long> documentosIds) {
        throw new UnsupportedOperationException(
                "Método ainda não implementado. Reutilize os métodos build* para completar.");
    }
}