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
import reset.reset.Models.core.Empresa;
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

    private static final DeviceRgb PRIMARY      = new DeviceRgb(30, 41, 59);
    private static final DeviceRgb SECONDARY    = new DeviceRgb(100, 116, 139);
    private static final DeviceRgb ACCENT       = new DeviceRgb(37, 99, 235);
    private static final DeviceRgb LIGHT_BG     = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb ALT_ROW      = new DeviceRgb(241, 245, 249);
    private static final DeviceRgb TOTAL_BG      = new DeviceRgb(226, 232, 240);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(203, 213, 225);
    private static final DeviceRgb WHITE        = new DeviceRgb(255, 255, 255);

    private static final float MARGIN = 42f;

    // Bordas apenas horizontais
    private static final Border NO_BORDER = Border.NO_BORDER;
    private static final Border H_BORDER = new SolidBorder(BORDER_COLOR, 0.6f);
    private static final Border H_BORDER_STRONG = new SolidBorder(PRIMARY, 1.0f);

    // ==================== MÉTODO PRINCIPAL ====================

    @Transactional(readOnly = true)
    public PDFResponse gerarPDFItext(Long documentoId, PDFConfigDTO config) {
        try {
            Documento documento = documentoRepository.findById(documentoId)
                    .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado: " + documentoId));

            Empresa empresa = documento.getEmpresa();

            PdfFont fontNormal = createFont(FONT_PATH);
            PdfFont fontBold = createFont(FONT_BOLD_PATH);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);

            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new PageFooterHandler(config, fontNormal));

            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(MARGIN, MARGIN, 70, MARGIN);

//            buildHeader(document, config, empresa, fontNormal, fontBold);
            buildTitleAndBadge(document, config, documento, fontBold);
            buildPartiesInfo(document, config, empresa, documento, fontNormal, fontBold);
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

    // ==================== SECÇÕES ====================

    private void buildHeader(Document doc, PDFConfigDTO config, Empresa empresa,
                             PdfFont normal, PdfFont bold) {

        Table header = new Table(UnitValue.createPercentArray(new float[]{1.2f, 2.8f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        // Logo
        Cell logoCell = new Cell()
                .setBorder(NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingRight(10);

        if (hasText(config.getLogoBase64())) {
            try {
                byte[] bytes = Base64.getDecoder().decode(config.getLogoBase64());
                Image logo = new Image(com.itextpdf.io.image.ImageDataFactory.create(bytes));
                logo.scaleToFit(100, 60);
                logoCell.add(logo);
            } catch (Exception e) {
                log.warn("Falha ao carregar logo: {}", e.getMessage());
            }
        }
        header.addCell(logoCell);

        // Dados da empresa (prioridade: config → entidade Empresa)
        Cell infoCell = new Cell()
                .setBorder(NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        String nomeEmpresa = firstNonBlank(config.getEmpresaNome(),
                empresa != null ? empresa.getNome() : null);
        String nuitEmpresa = firstNonBlank(config.getEmpresaNuit(),
                empresa != null ? empresa.getNuit() : null);
        String enderecoEmpresa = firstNonBlank(config.getEmpresaEndereco(),
                empresa != null ? empresa.getEndereco() : null);
        String telefoneEmpresa = firstNonBlank(config.getEmpresaTelefone(),
                empresa != null ? empresa.getTelefone() : null);
        String emailEmpresa = firstNonBlank(config.getEmpresaEmail(),
                empresa != null ? empresa.getEmail() : null);

        if (hasText(nomeEmpresa)) {
            infoCell.add(new Paragraph(nomeEmpresa)
                    .setFont(bold)
                    .setFontSize(13)
                    .setFontColor(PRIMARY)
                    .setMarginBottom(2));
        }

        addMutedLine(infoCell, hasText(nuitEmpresa) ? "NUIT: " + nuitEmpresa : null, normal);
        addMutedLine(infoCell, enderecoEmpresa, normal);
        addMutedLine(infoCell, hasText(telefoneEmpresa) ? "Tel: " + telefoneEmpresa : null, normal);
        addMutedLine(infoCell, hasText(emailEmpresa) ? "Email: " + emailEmpresa : null, normal);

        header.addCell(infoCell);
        doc.add(header);

        // Barra de destaque
        doc.add(new Div()
                .setHeight(2.2f)
                .setBackgroundColor(ACCENT)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16));
    }

    private void buildTitleAndBadge(Document doc, PDFConfigDTO config,
                                    Documento documento, PdfFont bold) {

        String titulo = hasText(config.getTitulo())
                ? config.getTitulo().toUpperCase()
                : "DOCUMENTO FISCAL";

        Table titleTable = new Table(UnitValue.createPercentArray(new float[]{2.4f, 1.6f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(14);

        Cell titleCell = new Cell()
                .setBorder(NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph(titulo)
                        .setFont(bold)
                        .setFontSize(14)
                        .setFontColor(PRIMARY));

        Cell badgeCell = new Cell()
                .setBorder(NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        Div badge = new Div()
                .setBackgroundColor(LIGHT_BG)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.7f))
                .setPadding(5)
                .setPaddingLeft(10)
                .setPaddingRight(10);

        badge.add(new Paragraph("Nº " + safe(documento.getNumero(), "—"))
                .setFont(bold)
                .setFontSize(10)
                .setFontColor(PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMargin(0));

        badgeCell.add(badge);
        titleTable.addCell(titleCell);
        titleTable.addCell(badgeCell);
        doc.add(titleTable);

    }

    /**
     * Secção com dados da Empresa (esquerda) e do Cliente (direita)
     */
    private void buildPartiesInfo(Document doc, PDFConfigDTO config, Empresa empresa,
                                  Documento documento, PdfFont normal, PdfFont bold) {

        boolean hasLogo = hasText(config.getLogoBase64());

        Table header;
        if (hasLogo) {
            header = new Table(UnitValue.createPercentArray(new float[]{1.15f, 1.85f, 2f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(10);
        } else {
            header = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(10);
        }

        // ========== LOGO (só se existir) ==========
        if (hasLogo) {
            Cell logoCell = new Cell()
                    .setBorder(NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPaddingRight(8)
                    .setPaddingBottom(4);

            try {
                byte[] bytes = Base64.getDecoder().decode(config.getLogoBase64());
                Image logo = new Image(com.itextpdf.io.image.ImageDataFactory.create(bytes));
                logo.scaleToFit(85, 55);
                logoCell.add(logo);
            } catch (Exception e) {
                log.warn("Falha ao carregar logo: {}", e.getMessage());
                logoCell.add(new Paragraph(""));
            }
            header.addCell(logoCell);
        }

        // ========== EMPRESA ==========
        Cell empresaCell = new Cell()
                .setBorder(NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(0)
                .setPaddingRight(8);

        empresaCell.add(new Paragraph("EMPRESA")
                .setFont(bold)
                .setFontSize(7.5f)
                .setFontColor(ACCENT)
                .setMarginBottom(3));

        String nomeEmp = firstNonBlank(config.getEmpresaNome(),
                empresa != null ? empresa.getNome() : null);
        String nuitEmp = firstNonBlank(config.getEmpresaNuit(),
                empresa != null ? empresa.getNuit() : null);
        String endEmp = firstNonBlank(config.getEmpresaEndereco(),
                empresa != null ? empresa.getEndereco() : null);
        String telEmp = firstNonBlank(config.getEmpresaTelefone(),
                empresa != null ? empresa.getTelefone() : null);
        String emailEmp = firstNonBlank(config.getEmpresaEmail(),
                empresa != null ? empresa.getEmail() : null);

//        addPartyLine(empresaCell, nomeEmp, bold, 8, PRIMARY);
        addPartyLine(empresaCell, hasText(nuitEmp) ? "NUIT: " + nuitEmp : null, normal, 7f, SECONDARY);
        addPartyLine(empresaCell, endEmp, normal, 8f, SECONDARY);
        addPartyLine(empresaCell, hasText(telEmp) ? "Tel: " + telEmp : null, normal, 7f, SECONDARY);
        addPartyLine(empresaCell, hasText(emailEmp) ? "Email: " + emailEmp : null, normal, 7f, SECONDARY);

        header.addCell(empresaCell);

        // ========== CLIENTE ==========
        Cell clienteCell = new Cell()
                .setBorder(NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(0)
                .setPaddingLeft(8)
                .setTextAlignment(TextAlignment.RIGHT);

        clienteCell.add(new Paragraph("CLIENTE")
                .setFont(bold)
                .setFontSize(7.5f)
                .setFontColor(ACCENT)
                .setMarginBottom(3)
                .setTextAlignment(TextAlignment.RIGHT));

        String nomeCli = "Cliente não informado";
        String nuitCli = null;
        String endCli = null;
        String telCli = null;

        if (documento.getCliente() != null) {
            nomeCli = safe(documento.getCliente().getNome(), "Cliente não informado");
            nuitCli = documento.getCliente().getNuit();
            endCli = safeGet(() -> documento.getCliente().getEndereco());
            telCli = safeGet(() -> documento.getCliente().getTelefone());
        }

//        addPartyLine(clienteCell, nomeCli, bold, 8, PRIMARY, TextAlignment.RIGHT);
        addPartyLine(clienteCell, hasText(nuitCli) ? "NUIT: " + nuitCli : null, normal, 7f, SECONDARY, TextAlignment.RIGHT);
        addPartyLine(clienteCell, endCli, normal, 7f, SECONDARY, TextAlignment.RIGHT);
        addPartyLine(clienteCell, hasText(telCli) ? "Tel: " + telCli : null, normal, 7f, SECONDARY, TextAlignment.RIGHT);

        String data = documento.getData() != null
                ? documento.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "—";
        addPartyLine(clienteCell, "Data: " + data, normal, 7f, SECONDARY, TextAlignment.RIGHT);

        header.addCell(clienteCell);
        doc.add(header);

        // Barra de destaque
        doc.add(new Div()
                .setHeight(1.8f)
                .setBackgroundColor(ACCENT)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(6)
                .setMarginBottom(16));
    }

    private void buildItemsTable(Document doc, Documento documento,
                                 PdfFont normal, PdfFont bold) {

        doc.add(new Paragraph("Detalhe dos Itens")
                .setFont(bold)
                .setFontSize(10)
                .setFontColor(PRIMARY)
                .setMarginBottom(6));

        Table table = new Table(UnitValue.createPercentArray(new float[]{34, 9, 17, 15, 15}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        // Cabeçalho – apenas linha inferior
        String[] headers = {"Descrição", "Qtd", "Preço Unit.", "Desconto", "Subtotal"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h)
                            .setFont(bold)
                            .setFontSize(8.5f)
                            .setFontColor(PRIMARY))
                    .setBackgroundColor(LIGHT_BG)
                    .setBorder(NO_BORDER)
                    .setBorderBottom(H_BORDER_STRONG)
                    .setPadding(7)
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
                    .setBorder(NO_BORDER)
                    .setBorderBottom(H_BORDER);
            table.addCell(empty);
        } else {
            int size = documento.getItens().size();
            int index = 0;

            for (DocumentoItem item : documento.getItens()) {
                DeviceRgb bg = alternate ? ALT_ROW : WHITE;
                alternate = !alternate;
                boolean isLast = (++index == size);

                String desc = item.getProduto() != null
                        ? safe(item.getProduto().getNome(), "Produto não informado")
                        : "Produto não informado";

                String qtd = item.getQuantidade() != null
                        ? item.getQuantidade().toString() : "0";

                String preco = item.getPrecoUnitario() != null
                        ? currency.format(item.getPrecoUnitario()) : "0,00";

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

                table.addCell(itemCell(desc, normal, TextAlignment.LEFT, bg, isLast));
                table.addCell(itemCell(qtd, normal, TextAlignment.CENTER, bg, isLast));
                table.addCell(itemCell(preco, normal, TextAlignment.RIGHT, bg, isLast));
                table.addCell(itemCell(desconto, normal, TextAlignment.RIGHT, bg, isLast));
                table.addCell(itemCell(currency.format(subtotal), bold, TextAlignment.RIGHT, bg, isLast));
            }
        }

        doc.add(table);
    }

    private void buildTotals(Document doc, Documento documento,
                             PdfFont normal, PdfFont bold) {

        NumberFormat currency = currencyFormat();
        BigDecimal total = documento.getTotal() != null ? documento.getTotal() : BigDecimal.ZERO;

        Table totals = new Table(UnitValue.createPercentArray(new float[]{1.5f, 1}))
                .setWidth(UnitValue.createPercentValue(40))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setMarginBottom(20);

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

    private void buildObservations(Document doc, Documento documento,
                                   PdfFont normal, PdfFont bold) {

        if (!hasText(documento.getObservacao())) {
            return;
        }

        doc.add(new Paragraph("Observações")
                .setFont(bold)
                .setFontSize(9)
                .setFontColor(PRIMARY)
                .setMarginBottom(4));

        Div box = new Div()
                .setBackgroundColor(LIGHT_BG)
                .setBorder(NO_BORDER)
                .setBorderLeft(new SolidBorder(ACCENT, 2.5f))
                .setPadding(8)
                .setPaddingLeft(12)
                .setMarginBottom(10);

        box.add(new Paragraph(documento.getObservacao())
                .setFont(normal)
                .setFontSize(8.5f)
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

            pdfCanvas.setStrokeColor(BORDER_COLOR)
                    .setLineWidth(0.5f)
                    .moveTo(MARGIN, y + 16)
                    .lineTo(pageSize.getWidth() - MARGIN, y + 16)
                    .stroke();

            String rodape = hasText(config.getRodape()) ? config.getRodape() : "";
            canvas.showTextAligned(
                    new Paragraph(rodape)
                            .setFont(font)
                            .setFontSize(7)
                            .setFontColor(SECONDARY),
                    MARGIN, y + 4, TextAlignment.LEFT);

            String gerado = "Gerado em " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            canvas.showTextAligned(
                    new Paragraph(gerado)
                            .setFont(font)
                            .setFontSize(7)
                            .setFontColor(SECONDARY),
                    pageSize.getWidth() / 2, y + 4, TextAlignment.CENTER);

            canvas.showTextAligned(
                    new Paragraph("Página " + pageNumber + " de " + totalPages)
                            .setFont(font)
                            .setFontSize(7)
                            .setFontColor(SECONDARY),
                    pageSize.getWidth() - MARGIN, y + 4, TextAlignment.RIGHT);

            canvas.close();
        }
    }

    // ==================== HELPERS DE UI ====================

    private Cell itemCell(String text, PdfFont font, TextAlignment align,
                          DeviceRgb bg, boolean isLast) {
        Cell cell = new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(8.5f)
                        .setFontColor(PRIMARY))
                .setBackgroundColor(bg)
                .setBorder(NO_BORDER)
                .setBorderBottom(isLast ? H_BORDER_STRONG : H_BORDER)
                .setPadding(6)
                .setTextAlignment(align)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        return cell;
    }

    private Cell totalLabel(String text, PdfFont font, boolean isTotal) {
        Cell cell = new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(isTotal ? 10 : 8.5f)
                        .setFontColor(isTotal ? PRIMARY : SECONDARY))
                .setBorder(NO_BORDER)
                .setPadding(5)
                .setTextAlignment(TextAlignment.RIGHT);

        if (isTotal) {
            cell.setBackgroundColor(TOTAL_BG)
                    .setBorderTop(H_BORDER_STRONG);
        }
        return cell;
    }

    private Cell totalValue(String text, PdfFont font, boolean isTotal) {
        Cell cell = new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(isTotal ? 10.5f : 8.5f)
                        .setFontColor(PRIMARY))
                .setBorder(NO_BORDER)
                .setPadding(5)
                .setTextAlignment(TextAlignment.RIGHT);

        if (isTotal) {
            cell.setBackgroundColor(TOTAL_BG)
                    .setBorderTop(H_BORDER_STRONG);
        }
        return cell;
    }

    private void addMutedLine(Cell cell, String text, PdfFont font) {
        if (!hasText(text)) return;
        cell.add(new Paragraph(text)
                .setFont(font)
                .setFontSize(8)
                .setFontColor(SECONDARY)
                .setMarginBottom(1));
    }

    private void addPartyLine(Cell cell, String text, PdfFont font,
                              float size, DeviceRgb color) {
        addPartyLine(cell, text, font, size, color, TextAlignment.LEFT);
    }

    private void addPartyLine(Cell cell, String text, PdfFont font,
                              float size, DeviceRgb color, TextAlignment align) {
        if (!hasText(text)) return;
        cell.add(new Paragraph(text)
                .setFont(font)
                .setFontSize(size)
                .setFontColor(color)
                .setTextAlignment(align)
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

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (hasText(v)) return v;
        }
        return null;
    }

    /** Evita NPE caso o getter não exista ou lance exceção */
    private static String safeGet(java.util.function.Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
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
                "Método ainda não implementado. Reutilize os métodos build*.");
    }
}