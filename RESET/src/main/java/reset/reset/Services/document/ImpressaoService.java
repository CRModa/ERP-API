package reset.reset.Services.document;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;
import reset.reset.Models.document.Documento;
import reset.reset.Models.document.DocumentoItem;
import reset.reset.Models.document.DocumentoTipo;
import reset.reset.Models.document.Tipos.Recibo;
import reset.reset.Models.restaurant.Pedido;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Repositories.restaurant.PedidoRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.dto.document.ReciboDTO;
import reset.reset.dto.document.pdf.ImpressaoRequest;
import reset.reset.dto.document.pdf.ReciboToPrint;
import reset.reset.utils.NumberToWordsUtils;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImpressaoService {

    private final DocumentoRepository documentoRepository;
    private final PedidoRepository pedidoRepository;
    private final EmpresaRepository empresaRepository;

    private static final String FONT_PATH = "fonts/DejaVuSans.ttf";
    private static final String FONT_BOLD_PATH = "fonts/DejaVuSans-Bold.ttf";

    // ==================== METODO PRINCIPAL ====================

    @Transactional(readOnly = true)
    public void imprimirRecibo(ImpressaoRequest request) {
        try {
            log.info("Iniciando impressão do recibo - Documento: {}, Pedido: {}",
                    request.getDocumentoId(), request.getPedidoId());

            // Busca os dados
            Documento documento = documentoRepository.findById(request.getDocumentoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Documento não encontrado: " + request.getDocumentoId()));

            Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Pedido não encontrado: " + request.getPedidoId()));

            Empresa empresa = documento.getEmpresa();
            if (empresa == null) {
                empresa = empresaRepository.findById(1L)
                        .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));
            }

            // Monta o DTO do recibo
            ReciboToPrint recibo = montarReciboDTO(documento, pedido, empresa);

            // Determina o formato de impressão
            String formato = request.getFormato() != null ? request.getFormato() : "TERMICA";

            if ("TERMICA".equalsIgnoreCase(formato)) {
                imprimirReciboTermica(recibo, request.getCopias());
            } else if ("PDF".equalsIgnoreCase(formato)) {
                imprimirReciboPDF(recibo);
            } else {
                throw new BusinessException("Formato de impressão não suportado: " + formato);
            }

            log.info("Recibo impresso com sucesso");

        } catch (Exception e) {
            log.error("Erro ao imprimir recibo: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao imprimir recibo: " + e.getMessage());
        }
    }

    // ==================== MONTAGEM DO DTO ====================

    private ReciboToPrint montarReciboDTO(Documento documento, Pedido pedido, Empresa empresa) {
        // Dados do Documento
        ReciboToPrint.DocumentoInfo docInfo = ReciboToPrint.DocumentoInfo.fromEntity(documento);

        // Verifica se é um Recibo (subclasse) para pegar dados adicionais
        if (documento instanceof Recibo) {
            Recibo recibo = (Recibo) documento;
            docInfo.setFormaPagamento(recibo.getFormaPagamento());
            docInfo.setDataPagamento(recibo.getDataPagamento());
            docInfo.setReferenciaPagamento(recibo.getReferenciaPagamento());
        }

        // Dados do Pedido
        ReciboToPrint.PedidoInfo pedidoInfo = ReciboToPrint.PedidoInfo.fromEntity(pedido);
        pedidoInfo.setTotalExtenso(NumberToWordsUtils.convert(pedido.getTotal()));

        // Dados do Pagamento
        ReciboToPrint.PagamentoInfo pagamentoInfo = ReciboToPrint.PagamentoInfo.builder()
                .forma(obterFormaPagamento(documento, pedido))
                .valor(pedido.getTotal())
                .valorPago(pedido.getTotal())
                .troco(BigDecimal.ZERO)
                .status("PAGO")
                .dataPagamento(LocalDateTime.now())
                .referencia(obterReferenciaPagamento(documento))
                .build();

        // Dados da Empresa
        ReciboToPrint.EmpresaInfo empresaInfo = ReciboToPrint.EmpresaInfo.fromEntity(empresa);

        // Dados do Cliente
        ReciboToPrint.ClienteInfo clienteInfo = null;
        if (documento.getCliente() != null) {
            clienteInfo = ReciboToPrint.ClienteInfo.fromEntity(documento.getCliente());
        } else if (pedido.getCliente() != null) {
            clienteInfo = ReciboToPrint.ClienteInfo.fromEntity(pedido.getCliente());
        }

        return ReciboToPrint.builder()
                .documentoId(documento.getId())
                .pedidoId(pedido.getId())
                .numeroRecibo("REC-" + documento.getId() + "-" + System.currentTimeMillis() % 1000000)
                .dataEmissao(LocalDateTime.now())
                .totalExtenso(NumberToWordsUtils.convert(documento.getTotal()))
                .documento(docInfo)
                .pedido(pedidoInfo)
                .pagamento(pagamentoInfo)
                .empresa(empresaInfo)
                .cliente(clienteInfo)
                .build();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String obterFormaPagamento(Documento documento, Pedido pedido) {
        if (documento instanceof Recibo) {
            Recibo recibo = (Recibo) documento;
            if (recibo.getFormaPagamento() != null) {
                return recibo.getFormaPagamento();
            }
        }
        return "NÃO INFORMADO";
    }

    private String obterReferenciaPagamento(Documento documento) {
        if (documento instanceof Recibo) {
            Recibo recibo = (Recibo) documento;
            if (recibo.getReferenciaPagamento() != null) {
                return recibo.getReferenciaPagamento();
            }
        }
        return null;
    }

    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) {
            return "0,00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "MZ"));
        symbols.setCurrencySymbol("MZN");
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat format = new DecimalFormat("#,##0.00 ¤", symbols);
        format.setCurrency(Currency.getInstance("MZN"));
        return format.format(valor);
    }

    // ==================== IMPRESSÃO TÉRMICA ====================

//    private void imprimirReciboTermica(ReciboToPrint recibo, Integer copias) {
//        try {
//            String textoRecibo = gerarTextoRecibo(recibo);
//
//            // Envia para impressora térmica
//            PrintService impressora = encontrarImpressoraTermica();
//
//            if (impressora == null) {
//                throw new BusinessException("Nenhuma impressora térmica encontrada");
//            }
//
//            DocPrintJob job = impressora.createPrintJob();
//            DocFlavor flavor = DocFlavor.STRING.TEXT_PLAIN;
//            Doc doc = new SimpleDoc(textoRecibo, flavor, null);
//
//            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
//            attrs.add(new Copies(copias != null ? copias : 1));
//            attrs.add(MediaSizeName.ISO_A4);
//
//            job.print(doc, attrs);
//
//            log.info("Recibo térmico enviado para impressora: {}", impressora.getName());
//
//        } catch (PrintException e) {
//            log.error("Erro na impressão térmica: {}", e.getMessage(), e);
//            throw new BusinessException("Erro ao imprimir recibo: " + e.getMessage());
//        }
//    }

    private void imprimirReciboTermica(ReciboToPrint recibo, Integer copias) {
        try {
            String textoRecibo = gerarTextoRecibo(recibo);

            // Converte para bytes com encoding correto
            byte[] bytes = textoRecibo.getBytes(StandardCharsets.UTF_8);

            PrintService impressora = encontrarImpressoraTermica();

            if (impressora == null) {
                throw new BusinessException("Nenhuma impressora térmica encontrada");
            }

            DocPrintJob job = impressora.createPrintJob();

            // Usa BYTE_ARRAY em vez de STRING
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(bytes, flavor, null);

            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            attrs.add(new Copies(copias != null ? copias : 1));

            job.print(doc, attrs);

            log.info("Recibo térmico enviado para impressora: {}", impressora.getName());

        } catch (PrintException e) {
            log.error("Erro na impressão térmica: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao imprimir recibo: " + e.getMessage());
        }
    }

    private String gerarTextoRecibo(ReciboToPrint recibo) {
        StringBuilder sb = new StringBuilder();

        // Reset e centralizar
        sb.append("\u001B@");
        sb.append("\u001B\u0061\u0001");

        // ========== CABEÇALHO ==========
        sb.append("================================\n");
        sb.append("           R E C I B O\n");
        sb.append("================================\n\n");

        // Dados da Empresa
        if (recibo.getEmpresa() != null) {
            ReciboToPrint.EmpresaInfo empresa = recibo.getEmpresa();
            sb.append(empresa.getNome() != null ? empresa.getNome() : "EMPRESA").append("\n");
            if (empresa.getEndereco() != null && !empresa.getEndereco().isEmpty()) {
                sb.append(empresa.getEndereco()).append("\n");
            }
            if (empresa.getNuit() != null && !empresa.getNuit().isEmpty()) {
                sb.append("NUIT: ").append(empresa.getNuit()).append("\n");
            }
            if (empresa.getTelefone() != null && !empresa.getTelefone().isEmpty()) {
                sb.append("Tel: ").append(empresa.getTelefone()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("--------------------------------\n");

        // ========== INFORMAÇÕES DO RECIBO ==========
        sb.append("Recibo: ").append(recibo.getNumeroRecibo()).append("\n");
        sb.append("Data: ").append(recibo.getDataEmissao() != null ?
                recibo.getDataEmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) :
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");

        if (recibo.getPedido() != null) {
            ReciboToPrint.PedidoInfo pedido = recibo.getPedido();
            sb.append("Pedido: #").append(pedido.getNumero() != null ? pedido.getNumero() : pedido.getId()).append("\n");
            sb.append("Mesa: ").append(pedido.getMesaNumero() != null ? pedido.getMesaNumero() : "---").append("\n");
            sb.append("Tipo: ").append(pedido.getTipo() != null ? pedido.getTipo() : "MESA").append("\n");
        }

        if (recibo.getDocumento() != null) {
            sb.append("Documento: ").append(recibo.getDocumento().getNumero()).append("\n");
            sb.append("Tipo: ").append(recibo.getDocumento().getTipo()).append("\n");
        }

        sb.append("--------------------------------\n");

        // ========== CLIENTE ==========
        if (recibo.getCliente() != null) {
            ReciboToPrint.ClienteInfo cliente = recibo.getCliente();
            sb.append("Cliente: ").append(cliente.getNome()).append("\n");
            if (cliente.getNuit() != null && !cliente.getNuit().isEmpty()) {
                sb.append("NUIT: ").append(cliente.getNuit()).append("\n");
            }
            if (cliente.getEndereco() != null && !cliente.getEndereco().isEmpty()) {
                sb.append("End: ").append(cliente.getEndereco()).append("\n");
            }
            sb.append("--------------------------------\n");
        }

        // ========== ITENS ==========
        sb.append("ITEM                QTD  PREÇO   SUBTOTAL\n");
        sb.append("--------------------------------\n");

        if (recibo.getPedido() != null && recibo.getPedido().getItens() != null) {
            for (ReciboToPrint.ItemInfo item : recibo.getPedido().getItens()) {
                String nome = item.getProdutoNome();
                if (nome.length() > 15) {
                    nome = nome.substring(0, 15);
                }
                if (Boolean.TRUE.equals(item.getIsComposto())) {
                    nome = nome + " *";
                }
                String linha = String.format("%-15s %3d %7.2f %9.2f\n",
                        nome,
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal());
                sb.append(linha);
            }
        }

        sb.append("--------------------------------\n");

        // ========== TOTAIS ==========
        if (recibo.getPedido() != null) {
            ReciboToPrint.PedidoInfo pedido = recibo.getPedido();
            sb.append(String.format("SUBTOTAL: %20.2f\n",
                    pedido.getSubtotal() != null ? pedido.getSubtotal() : BigDecimal.ZERO));

            if (pedido.getDesconto() != null && pedido.getDesconto().compareTo(BigDecimal.ZERO) > 0) {
                sb.append(String.format("DESCONTO: %21.2f\n", pedido.getDesconto()));
            }

            if (pedido.getTaxaServico() != null && pedido.getTaxaServico().compareTo(BigDecimal.ZERO) > 0) {
                sb.append(String.format("TAXA SERV. (10%%): %16.2f\n", pedido.getTaxaServico()));
            }

            sb.append("================================\n");
            sb.append(String.format("TOTAL: %24.2f\n",
                    pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO));
        }

        // ========== VALOR POR EXTENSO ==========
        if (recibo.getTotalExtenso() != null) {
            sb.append(recibo.getTotalExtenso()).append("\n");
        }

        // ========== PAGAMENTO ==========
        if (recibo.getPagamento() != null) {
            ReciboToPrint.PagamentoInfo pagamento = recibo.getPagamento();
            sb.append("--------------------------------\n");
            sb.append("PAGAMENTO: ").append(pagamento.getForma()).append("\n");
            sb.append("STATUS: ").append(pagamento.getStatus()).append("\n");
            if (pagamento.getTroco() != null && pagamento.getTroco().compareTo(BigDecimal.ZERO) > 0) {
                sb.append(String.format("TROCO: %.2f\n", pagamento.getTroco()));
            }
            if (pagamento.getReferencia() != null && !pagamento.getReferencia().isEmpty()) {
                sb.append("REF: ").append(pagamento.getReferencia()).append("\n");
            }
        }

        // ========== OBSERVAÇÕES ==========
        String observacao = null;
        if (recibo.getPedido() != null && recibo.getPedido().getObservacao() != null) {
            observacao = recibo.getPedido().getObservacao();
        } else if (recibo.getDocumento() != null && recibo.getDocumento().getObservacao() != null) {
            observacao = recibo.getDocumento().getObservacao();
        }

        if (observacao != null && !observacao.isEmpty()) {
            sb.append("--------------------------------\n");
            sb.append("Obs: ").append(observacao).append("\n");
        }

        // ========== RODAPÉ ==========
        sb.append("================================\n");
        sb.append("    OBRIGADO PELA PREFERÊNCIA!\n");
        sb.append("================================\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n"); // Espaço para corte

        return sb.toString();
    }

    private PrintService encontrarImpressoraTermica() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService service : services) {
            String nome = service.getName().toLowerCase();
            if (nome.contains("termica") || nome.contains("thermal") ||
                    nome.contains("pos") || nome.contains("ec-") ||
                    nome.contains("e-pos") || nome.contains("epson") ||
                    nome.contains("bematech") || nome.contains("daruma") ||
                    nome.contains("mp-") || nome.contains("printer") ||
                    nome.contains("80mm")) {
                log.info("Impressora térmica encontrada: {}", service.getName());
                return service;
            }
        }

        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultService != null) {
            log.info("Usando impressora padrão: {}", defaultService.getName());
        }
        return defaultService;
    }

    // ==================== IMPRESSÃO PDF ====================

    private void imprimirReciboPDF(ReciboToPrint recibo) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);

            Document doc = new Document(pdfDoc);
            doc.setMargins(40, 40, 40, 40);

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

            // Título
            doc.add(new Paragraph("RECIBO")
                    .setFont(fontBold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Empresa
            if (recibo.getEmpresa() != null) {
                ReciboToPrint.EmpresaInfo empresa = recibo.getEmpresa();
                doc.add(new Paragraph(empresa.getNome())
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER));

                if (empresa.getEndereco() != null) {
                    doc.add(new Paragraph(empresa.getEndereco())
                            .setFont(fontNormal)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));
                }

                doc.add(new Paragraph("NUIT: " + (empresa.getNuit() != null ? empresa.getNuit() : "---"))
                        .setFont(fontNormal)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));

                if (empresa.getTelefone() != null) {
                    doc.add(new Paragraph("Tel: " + empresa.getTelefone())
                            .setFont(fontNormal)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));
                }
            }

            doc.add(new Paragraph("--------------------------------------------------")
                    .setFont(fontNormal)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10)
                    .setMarginBottom(10));

            // Informações
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            addInfoRow(infoTable, "Recibo:", recibo.getNumeroRecibo(), fontNormal);
            addInfoRow(infoTable, "Data:", recibo.getDataEmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontNormal);

            if (recibo.getPedido() != null) {
                addInfoRow(infoTable, "Pedido:", "#" + recibo.getPedido().getNumero(), fontNormal);
                addInfoRow(infoTable, "Mesa:", recibo.getPedido().getMesaNumero(), fontNormal);
            }

            if (recibo.getDocumento() != null) {
                addInfoRow(infoTable, "Documento:", recibo.getDocumento().getNumero(), fontNormal);
                addInfoRow(infoTable, "Tipo:", recibo.getDocumento().getTipo(), fontNormal);
            }

            doc.add(infoTable);

            // Cliente
            if (recibo.getCliente() != null) {
                ReciboToPrint.ClienteInfo cliente = recibo.getCliente();
                doc.add(new Paragraph("Cliente: " + cliente.getNome())
                        .setFont(fontNormal)
                        .setFontSize(10)
                        .setMarginTop(5));
                if (cliente.getNuit() != null) {
                    doc.add(new Paragraph("NUIT: " + cliente.getNuit())
                            .setFont(fontNormal)
                            .setFontSize(10));
                }
            }

            // Itens
            doc.add(new Paragraph("ITENS")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setMarginTop(15)
                    .setMarginBottom(10));

            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{3f, 1f, 1.5f, 1.5f}))
                    .setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"Descrição", "Qtd", "Preço", "Subtotal"};
            for (String header : headers) {
                itemsTable.addCell(new Cell()
                        .add(new Paragraph(header).setFont(fontBold).setFontSize(9))
                        .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            }

            if (recibo.getPedido() != null && recibo.getPedido().getItens() != null) {
                for (ReciboToPrint.ItemInfo item : recibo.getPedido().getItens()) {
                    String nome = item.getProdutoNome();
                    if (Boolean.TRUE.equals(item.getIsComposto())) {
                        nome = nome + " *";
                    }
                    itemsTable.addCell(new Cell().add(new Paragraph(nome).setFont(fontNormal).setFontSize(9)));
                    itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantidade())).setFont(fontNormal).setFontSize(9)));
                    itemsTable.addCell(new Cell().add(new Paragraph(formatarMoeda(item.getPrecoUnitario())).setFont(fontNormal).setFontSize(9)));
                    itemsTable.addCell(new Cell().add(new Paragraph(formatarMoeda(item.getSubtotal())).setFont(fontNormal).setFontSize(9)));
                }
            }

            doc.add(itemsTable);

            // Totais
            if (recibo.getPedido() != null) {
                ReciboToPrint.PedidoInfo pedido = recibo.getPedido();
                doc.add(new Paragraph("--------------------------------------------------")
                        .setFont(fontNormal)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMarginTop(10));

                doc.add(new Paragraph("Subtotal: " + formatarMoeda(pedido.getSubtotal()))
                        .setFont(fontNormal)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));

                if (pedido.getDesconto() != null && pedido.getDesconto().compareTo(BigDecimal.ZERO) > 0) {
                    doc.add(new Paragraph("Desconto: -" + formatarMoeda(pedido.getDesconto()))
                            .setFont(fontNormal)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.RED));
                }

                if (pedido.getTaxaServico() != null && pedido.getTaxaServico().compareTo(BigDecimal.ZERO) > 0) {
                    doc.add(new Paragraph("Taxa Serviço (10%): " + formatarMoeda(pedido.getTaxaServico()))
                            .setFont(fontNormal)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.RIGHT));
                }

                doc.add(new Paragraph("TOTAL: " + formatarMoeda(pedido.getTotal()))
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMarginTop(5));
            }

            // Valor por extenso
            if (recibo.getTotalExtenso() != null) {
                doc.add(new Paragraph("Valor por extenso: " + recibo.getTotalExtenso())
                        .setFont(fontNormal)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));
            }

            // Pagamento
            if (recibo.getPagamento() != null) {
                ReciboToPrint.PagamentoInfo pagamento = recibo.getPagamento();
                doc.add(new Paragraph("Forma de Pagamento: " + pagamento.getForma())
                        .setFont(fontNormal)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));

                if (pagamento.getReferencia() != null) {
                    doc.add(new Paragraph("Referência: " + pagamento.getReferencia())
                            .setFont(fontNormal)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.RIGHT));
                }
            }

            // Observações
            String obs = null;
            if (recibo.getPedido() != null && recibo.getPedido().getObservacao() != null) {
                obs = recibo.getPedido().getObservacao();
            } else if (recibo.getDocumento() != null && recibo.getDocumento().getObservacao() != null) {
                obs = recibo.getDocumento().getObservacao();
            }

            if (obs != null && !obs.isEmpty()) {
                doc.add(new Paragraph("Observações: " + obs)
                        .setFont(fontNormal)
                        .setFontSize(9)
                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                        .setMarginTop(10));
            }

            // Rodapé
            doc.add(new Paragraph("Documento gerado eletronicamente. Obrigado pela preferência!")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                    .setMarginTop(30));

            doc.close();

            byte[] pdfBytes = baos.toByteArray();
            log.info("Recibo PDF gerado com sucesso. Tamanho: {} bytes", pdfBytes.length);

        } catch (Exception e) {
            log.error("Erro ao gerar PDF do recibo: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao gerar PDF do recibo: " + e.getMessage());
        }
    }

    private void addInfoRow(Table table, String label, String value, PdfFont font) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(font).setFontSize(9))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        table.addCell(new Cell()
                .add(new Paragraph(value != null ? value : "---").setFont(font).setFontSize(9))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
    }

    // ==================== MÉTODOS DE UTILIDADE ====================

    public boolean isImpressoraDisponivel() {
        try {
            PrintService impressora = encontrarImpressoraTermica();
            return impressora != null;
        } catch (Exception e) {
            log.error("Erro ao verificar disponibilidade da impressora: {}", e.getMessage());
            return false;
        }
    }

    public List<String> listarImpressoras() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        return java.util.Arrays.stream(services)
                .map(PrintService::getName)
                .collect(Collectors.toList());
    }
}