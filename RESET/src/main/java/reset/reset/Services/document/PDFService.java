package reset.reset.Services.document;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFService {

    private final DocumentoRepository documentoRepository;
    private final EmpresaRepository empresaRepository;
    private final TemplateEngine templateEngine;

    private static final String FONT_PATH = "fonts/DejaVuSans.ttf";
    private static final String FONT_BOLD_PATH = "fonts/DejaVuSans-Bold.ttf";

    // ==================== MÉTODOS PRINCIPAIS ====================

    /**
     * Gera PDF de um documento usando iText 7
     */
    @Transactional(readOnly = true)
    public PDFResponse gerarPDFItext(Long documentoId, PDFConfigDTO config) {
        try {
            Documento documento = documentoRepository.findById(documentoId)
                    .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado: " + documentoId));

            Empresa empresa = documento.getEmpresa();

            // Configurar fontes
            PdfFont fontNormal = PdfFontFactory.createFont(
                    new ClassPathResource(FONT_PATH).getPath(),
                    "Identity-H",
                    PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
            );

            PdfFont fontBold = PdfFontFactory.createFont(
                    new ClassPathResource(FONT_BOLD_PATH).getPath(),
                    "Identity-H",
                    PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
            );

            // Criar documento PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            // Margens: top, right, bottom, left
            document.setMargins(50, 50, 50, 50);

            // ===== CABEÇALHO =====
            // Logo e informações da empresa em tabela
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setMarginBottom(10);

            // Logo - Coluna esquerda
            Cell logoCell = new Cell();
            logoCell.setBorder(Border.NO_BORDER);
            if (config.getLogoBase64() != null && !config.getLogoBase64().isEmpty()) {
                try {
                    byte[] logoBytes = Base64.getDecoder().decode(config.getLogoBase64());
                    Image logo = new Image(com.itextpdf.io.image.ImageDataFactory.create(logoBytes));
                    logo.scaleToFit(100, 80);
                    logoCell.add(logo);
                } catch (Exception e) {
                    log.warn("Erro ao carregar logo: {}", e.getMessage());
                    logoCell.add(new Paragraph(" "));
                }
            } else {
                logoCell.add(new Paragraph(" "));
            }
            headerTable.addCell(logoCell);

            // Informações da empresa - Coluna direita
            Cell empresaCell = new Cell();
            empresaCell.setBorder(Border.NO_BORDER);
            empresaCell.setTextAlignment(TextAlignment.RIGHT);
            empresaCell.setVerticalAlignment(VerticalAlignment.MIDDLE);

            if (config.getEmpresaNome() != null) {
                empresaCell.add(new Paragraph(config.getEmpresaNome())
                        .setFont(fontBold)
                        .setFontSize(16));
            }

            if (config.getEmpresaNuit() != null) {
                empresaCell.add(new Paragraph("NUIT: " + config.getEmpresaNuit())
                        .setFont(fontNormal)
                        .setFontSize(10));
            }

            if (config.getEmpresaEndereco() != null) {
                empresaCell.add(new Paragraph(config.getEmpresaEndereco())
                        .setFont(fontNormal)
                        .setFontSize(10));
            }

            if (config.getEmpresaTelefone() != null) {
                empresaCell.add(new Paragraph("Tel: " + config.getEmpresaTelefone())
                        .setFont(fontNormal)
                        .setFontSize(10));
            }

            if (config.getEmpresaEmail() != null) {
                empresaCell.add(new Paragraph("Email: " + config.getEmpresaEmail())
                        .setFont(fontNormal)
                        .setFontSize(10));
            }

            headerTable.addCell(empresaCell);
            document.add(headerTable);

            // Linha separadora
            Div lineDiv = new Div();
            lineDiv.setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(1));
            lineDiv.setWidth(UnitValue.createPercentValue(100));
            lineDiv.setHeight(1);
            document.add(lineDiv);

            // ===== TÍTULO DO DOCUMENTO =====
            Paragraph titulo = new Paragraph(
                    config.getTitulo() != null ? config.getTitulo() : "DOCUMENTO FISCAL"
            );
            titulo.setFont(fontBold);
            titulo.setFontSize(16);
            titulo.setTextAlignment(TextAlignment.CENTER);
            titulo.setMarginBottom(10);
            document.add(titulo);

            // ===== INFORMAÇÕES DO DOCUMENTO =====
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            infoTable.setWidth(UnitValue.createPercentValue(100));
            infoTable.setMarginBottom(10);

            // Número do documento
            Cell cellNumero = new Cell();
            cellNumero.setBorder(Border.NO_BORDER);
            cellNumero.setPadding(2);
            cellNumero.add(new Paragraph("Nº Documento: ")
                    .setFont(fontBold)
                    .setFontSize(10));
            cellNumero.add(new Paragraph(documento.getNumero() != null ? documento.getNumero() : "-")
                    .setFont(fontNormal)
                    .setFontSize(10));
            infoTable.addCell(cellNumero);

            // Data
            Cell cellData = new Cell();
            cellData.setBorder(Border.NO_BORDER);
            cellData.setPadding(2);
            cellData.setTextAlignment(TextAlignment.RIGHT);
            cellData.add(new Paragraph("Data: ")
                    .setFont(fontBold)
                    .setFontSize(10));
            cellData.add(new Paragraph(
                    documento.getData() != null ?
                            documento.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"
            ).setFont(fontNormal).setFontSize(10));
            infoTable.addCell(cellData);

            // Cliente
            Cell cellCliente = new Cell();
            cellCliente.setBorder(Border.NO_BORDER);
            cellCliente.setPadding(2);
//            cellCliente.setColspan(2);
            cellCliente.add(new Paragraph("Cliente: ")
                    .setFont(fontBold)
                    .setFontSize(10));
            cellCliente.add(new Paragraph(
                    documento.getCliente() != null ? documento.getCliente().getNome() : "Cliente não informado"
            ).setFont(fontNormal).setFontSize(10));
            infoTable.addCell(cellCliente);

            // NUIT do Cliente
            if (documento.getCliente() != null && documento.getCliente().getNuit() != null) {
                Cell cellNuit = new Cell();
                cellNuit.setBorder(Border.NO_BORDER);
                cellNuit.setPadding(2);
//                cellNuit.setColspan(2);
                cellNuit.add(new Paragraph("NUIT Cliente: ")
                        .setFont(fontBold)
                        .setFontSize(10));
                cellNuit.add(new Paragraph(documento.getCliente().getNuit())
                        .setFont(fontNormal)
                        .setFontSize(10));
                infoTable.addCell(cellNuit);
            }

            document.add(infoTable);

            // ===== ITENS DO DOCUMENTO =====
            Paragraph itensTitle = new Paragraph("Itens do Documento")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setMarginBottom(5);
            document.add(itensTitle);

            // Tabela de itens
            Table itensTable = new Table(UnitValue.createPercentArray(new float[]{30, 10, 15, 15, 30}));
            itensTable.setWidth(UnitValue.createPercentValue(100));
            itensTable.setMarginBottom(10);

            // Cabeçalho da tabela
            String[] headers = {"Descrição", "Qtd", "Preço Unit.", "Desconto", "Subtotal"};
            for (String header : headers) {
                Cell headerCell = new Cell();
                headerCell.add(new Paragraph(header)
                        .setFont(fontBold)
                        .setFontSize(10));
                headerCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                headerCell.setPadding(5);
                headerCell.setTextAlignment(TextAlignment.CENTER);
                itensTable.addCell(headerCell);
            }

            // Itens
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "MZ"));
            for (DocumentoItem item : documento.getItens()) {
                // Descrição
                Cell descCell = new Cell();
                descCell.add(new Paragraph(
                        item.getProduto() != null ? item.getProduto().getNome() : "Produto não informado"
                ).setFont(fontNormal).setFontSize(10));
                descCell.setPadding(5);
                itensTable.addCell(descCell);

                // Quantidade
                Cell qtdCell = new Cell();
                qtdCell.add(new Paragraph(
                        item.getQuantidade() != null ? item.getQuantidade().toString() : "0"
                ).setFont(fontNormal).setFontSize(10));
                qtdCell.setPadding(5);
                qtdCell.setTextAlignment(TextAlignment.CENTER);
                itensTable.addCell(qtdCell);

                // Preço Unitário
                Cell precoCell = new Cell();
                precoCell.add(new Paragraph(
                        item.getPrecoUnitario() != null ?
                                currencyFormat.format(item.getPrecoUnitario()) : "0.00"
                ).setFont(fontNormal).setFontSize(10));
                precoCell.setPadding(5);
                precoCell.setTextAlignment(TextAlignment.RIGHT);
                itensTable.addCell(precoCell);

                // Desconto
                Cell descCellValor = new Cell();
                descCellValor.add(new Paragraph(
                        item.getDesconto() != null && item.getDesconto().getValor().compareTo(BigDecimal.ZERO) > 0 ?
                                currencyFormat.format(item.getDesconto().getValor()) : "-"
                ).setFont(fontNormal).setFontSize(10));
                descCellValor.setPadding(5);
                descCellValor.setTextAlignment(TextAlignment.RIGHT);
                itensTable.addCell(descCellValor);

                // Subtotal
                BigDecimal subtotal = item.getPrecoUnitario().multiply(item.getQuantidade());
                if (item.getDesconto() != null) {
                    subtotal = subtotal.subtract(item.getDesconto().getValor());
                }
                Cell subtotalCell = new Cell();
                subtotalCell.add(new Paragraph(
                        currencyFormat.format(subtotal)
                ).setFont(fontBold).setFontSize(10));
                subtotalCell.setPadding(5);
                subtotalCell.setTextAlignment(TextAlignment.RIGHT);
                itensTable.addCell(subtotalCell);
            }

            document.add(itensTable);

            // ===== TOTAIS =====
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            totalTable.setWidth(UnitValue.createPercentValue(50));
            totalTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);
            totalTable.setMarginBottom(10);

            // Subtotal
            Cell subTotalLabel = new Cell();
            subTotalLabel.add(new Paragraph("Subtotal:")
                    .setFont(fontBold)
                    .setFontSize(10));
            subTotalLabel.setBorder(Border.NO_BORDER);
            subTotalLabel.setPadding(5);
            subTotalLabel.setTextAlignment(TextAlignment.RIGHT);
            totalTable.addCell(subTotalLabel);

            Cell subTotalValor = new Cell();
            subTotalValor.add(new Paragraph(
                    currencyFormat.format(documento.getTotal() != null ? documento.getTotal() : BigDecimal.ZERO)
            ).setFont(fontNormal).setFontSize(10));
            subTotalValor.setBorder(Border.NO_BORDER);
            subTotalValor.setPadding(5);
            subTotalValor.setTextAlignment(TextAlignment.RIGHT);
            totalTable.addCell(subTotalValor);

            // Desconto
            if (documento.getCliente().getDescontoPadrao() != null && documento.getCliente().getDescontoPadrao().compareTo(BigDecimal.ZERO) > 0) {
                Cell descTotalLabel = new Cell();
                descTotalLabel.add(new Paragraph("Desconto:")
                        .setFont(fontBold)
                        .setFontSize(10));
                descTotalLabel.setBorder(Border.NO_BORDER);
                descTotalLabel.setPadding(5);
                descTotalLabel.setTextAlignment(TextAlignment.RIGHT);
                totalTable.addCell(descTotalLabel);

                Cell descTotalValor = new Cell();
                descTotalValor.add(new Paragraph(
                        "- " + currencyFormat.format(documento.getCliente().getDescontoPadrao())
                ).setFont(fontNormal).setFontSize(10));
                descTotalValor.setBorder(Border.NO_BORDER);
                descTotalValor.setPadding(5);
                descTotalValor.setTextAlignment(TextAlignment.RIGHT);
                totalTable.addCell(descTotalValor);
            }

            // Taxa de Serviço
//            if (documento.getTaxaServico() != null && documento.getTaxaServico().compareTo(BigDecimal.ZERO) > 0) {
//                Cell taxaLabel = new Cell();
//                taxaLabel.add(new Paragraph("Taxa de Serviço:")
//                        .setFont(fontBold)
//                        .setFontSize(10));
//                taxaLabel.setBorder(Border.NO_BORDER);
//                taxaLabel.setPadding(5);
//                taxaLabel.setTextAlignment(TextAlignment.RIGHT);
//                totalTable.addCell(taxaLabel);
//
//                Cell taxaValor = new Cell();
//                taxaValor.add(new Paragraph(
//                        currencyFormat.format(documento.getTaxaServico())
//                ).setFont(fontNormal).setFontSize(10));
//                taxaValor.setBorder(Border.NO_BORDER);
//                taxaValor.setPadding(5);
//                taxaValor.setTextAlignment(TextAlignment.RIGHT);
//                totalTable.addCell(taxaValor);
//            }

            // Total
            Cell totalLabel = new Cell();
            totalLabel.add(new Paragraph("TOTAL:")
                    .setFont(fontBold)
                    .setFontSize(10));
            totalLabel.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            totalLabel.setPadding(5);
            totalLabel.setTextAlignment(TextAlignment.RIGHT);
            totalTable.addCell(totalLabel);

            Cell totalValor = new Cell();
            totalValor.add(new Paragraph(
                    currencyFormat.format(documento.getTotal() != null ? documento.getTotal() : BigDecimal.ZERO)
            ).setFont(fontBold).setFontSize(10));
            totalValor.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            totalValor.setPadding(5);
            totalValor.setTextAlignment(TextAlignment.RIGHT);
            totalTable.addCell(totalValor);

            document.add(totalTable);

            // ===== OBSERVAÇÕES =====
            if (documento.getObservacao() != null && !documento.getObservacao().isEmpty()) {
                Paragraph observacao = new Paragraph();
                observacao.setMarginTop(10);
                observacao.add(new Paragraph("Observações: ")
                        .setFont(fontBold)
                        .setFontSize(10));
                observacao.add(new Paragraph(documento.getObservacao())
                        .setFont(fontNormal)
                        .setFontSize(10));
                document.add(observacao);
            }

            // ===== RODAPÉ =====
            if (config.getRodape() != null) {
                Paragraph rodape = new Paragraph(config.getRodape())
                        .setFont(fontNormal)
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20);
                document.add(rodape);
            }

            // Data de impressão
            Paragraph dataImpressao = new Paragraph(
                    "Documento gerado em: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5);
            document.add(dataImpressao);

            // Fechar documento
            document.close();

            // Preparar resposta
            byte[] pdfBytes = outputStream.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(pdfBytes);

            return PDFResponse.builder()
                    .fileName("documento_" + documento.getNumero() + ".pdf")
                    .fileBase64(base64)
                    .contentType("application/pdf")
                    .fileSize((long) pdfBytes.length)
                    .downloadUrl("/api/documentos/" + documentoId + "/pdf/download")
                    .build();

        } catch (Exception e) {
            log.error("Erro ao gerar PDF: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    /**
     * Gera PDF usando HTML Template (Thymeleaf) com iText 7 pdfHTML
     */
    @Transactional(readOnly = true)
    public byte[] gerarPDFHtml(Long documentoId) {
        try {
            Documento documento = documentoRepository.findById(documentoId)
                    .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado"));

            // Preparar contexto Thymeleaf
            Context context = new Context();
            context.setVariable("documento", documento);
            context.setVariable("cliente", documento.getCliente());
            context.setVariable("empresa", documento.getEmpresa());
            context.setVariable("itens", documento.getItens());
            context.setVariable("dataFormatada",
                    documento.getData() != null ?
                            documento.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");

            // Configurar formatação de moeda
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "MZ"));
            context.setVariable("formatCurrency", currencyFormat);

            // Renderizar HTML
            String html = templateEngine.process("documento-pdf", context);

            // Converter HTML para PDF usando iText 7 pdfHTML
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);

            // Configurar PDF
            pdfDoc.setDefaultPageSize(PageSize.A4);

            // Converter HTML para PDF
            ConverterProperties converterProperties = new ConverterProperties();
            converterProperties.setBaseUri("classpath:/templates/");

            HtmlConverter.convertToPdf(html, pdfDoc, converterProperties);

            pdfDoc.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Erro ao gerar PDF HTML: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    /**
     * Gera PDF para download direto
     */
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadPDF(Long documentoId, PDFConfigDTO config) {
        PDFResponse response = gerarPDFItext(documentoId, config);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + response.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(Base64.getDecoder().decode(response.getFileBase64()));
    }

    /**
     * Gera PDF em memória para múltiplos documentos
     */
    @Transactional(readOnly = true)
    public byte[] gerarPDFMultiplos(java.util.List<Long> documentosIds) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            boolean first = true;
            for (Long id : documentosIds) {
                Documento documento = documentoRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado: " + id));

                if (!first) {
                    document.add(new Paragraph("")); // Quebra de página
                    pdfDoc.addNewPage();
                }

                // Adicionar cada documento como uma seção
                // (Implementação similar ao gerarPDFItext, mas sem fechar o documento)

                first = false;
            }

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Erro ao gerar PDF múltiplos: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao gerar PDF: " + e.getMessage());
        }
    }
}