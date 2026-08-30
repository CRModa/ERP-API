package reset.reset.dto.document.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.customer.Cliente;
import reset.reset.Models.document.Documento;
import reset.reset.Models.restaurant.ItemPedido;
import reset.reset.Models.restaurant.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReciboToPrint {

    private Long documentoId;
    private Long pedidoId;
    private String numeroRecibo;
    private LocalDateTime dataEmissao;
    private String totalExtenso;

    // Dados do Documento
    private DocumentoInfo documento;

    // Dados do Pedido
    private PedidoInfo pedido;

    // Dados do Pagamento
    private PagamentoInfo pagamento;

    // Dados da Empresa
    private EmpresaInfo empresa;

    // Dados do Cliente
    private ClienteInfo cliente;

    // ========== CLASSES INTERNAS ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentoInfo {
        private Long id;
        private String numero;
        private String tipo;
        private BigDecimal total;
        private LocalDateTime data;
        private String observacao;
        private String estado;
        private String formaPagamento;
        private LocalDateTime dataPagamento;
        private String referenciaPagamento;

        public static DocumentoInfo fromEntity(Documento documento) {
            if (documento == null) return null;

            return DocumentoInfo.builder()
                    .id(documento.getId())
                    .numero(documento.getNumero())
                    .tipo(documento.getTipo() != null ? documento.getTipo().getDescricao() : "DOCUMENTO")
                    .total(documento.getTotal())
                    .data(documento.getData() != null ? documento.getData().atStartOfDay() : null)
                    .observacao(documento.getObservacao())
                    .estado(documento.getEstado())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PedidoInfo {
        private Long id;
        private String numero;
        private Long mesaId;
        private String mesaNumero;
        private String tipo;
        private String status;
        private BigDecimal subtotal;
        private BigDecimal desconto;
        private BigDecimal taxaServico;
        private BigDecimal total;
        private String observacao;
        private LocalDateTime dataPedido;
        private String totalExtenso;
        private List<ItemInfo> itens;

        public static PedidoInfo fromEntity(Pedido pedido) {
            if (pedido == null) return null;

            return PedidoInfo.builder()
                    .id(pedido.getId())
                    .numero(pedido.getNumero())
                    .mesaId(pedido.getMesa() != null ? pedido.getMesa().getId() : null)
                    .mesaNumero(pedido.getMesa() != null ? pedido.getMesa().getNumero() : "---")
                    .tipo(pedido.getTipo() != null ? pedido.getTipo().toString() : "MESA")
                    .status(pedido.getStatus() != null ? pedido.getStatus().toString() : "PENDENTE")
                    .subtotal(pedido.getSubtotal())
                    .desconto(pedido.getDesconto())
                    .taxaServico(pedido.getTaxaServico())
                    .total(pedido.getTotal())
                    .observacao(pedido.getObservacao())
                    .dataPedido(pedido.getDataPedido())
                    .itens(pedido.getItens() != null ?
                            pedido.getItens().stream()
                                    .map(ItemInfo::fromEntity)
                                    .collect(Collectors.toList()) :
                            List.of())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemInfo {
        private Long id;
        private Long produtoId;
        private String produtoNome;
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal subtotal;
        private Boolean isComposto;
        private String observacao;

        public static ItemInfo fromEntity(ItemPedido item) {
            if (item == null) return null;

            return ItemInfo.builder()
                    .id(item.getId())
                    .produtoId(item.getProduto() != null ? item.getProduto().getId() : null)
                    .produtoNome(item.getProduto() != null ? item.getProduto().getNome() : "Produto não informado")
                    .quantidade(item.getQuantidade().intValue())
                    .precoUnitario(item.getPrecoUnitario())
                    .subtotal(item.getSubtotal())
                    .isComposto(item.getProduto().getIsComposto() != null ? item.getProduto().getIsComposto() : false)
                    .observacao(item.getObservacao())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagamentoInfo {
        private String forma;
        private BigDecimal valor;
        private BigDecimal valorPago;
        private BigDecimal troco;
        private String status;
        private LocalDateTime dataPagamento;
        private String referencia;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmpresaInfo {
        private Long id;
        private String nome;
        private String nuit;
        private String endereco;
        private String telefone;
        private String email;
        private String moeda;
        private String logotipo;

        public static EmpresaInfo fromEntity(Empresa empresa) {
            if (empresa == null) return null;

            return EmpresaInfo.builder()
                    .id(empresa.getId())
                    .nome(empresa.getNome())
                    .nuit(empresa.getNuit())
                    .endereco(empresa.getEndereco())
                    .telefone(empresa.getTelefone())
                    .email(empresa.getEmail())
                    .moeda(empresa.getMoeda() != null ? empresa.getMoeda() : "MZN")
                    .logotipo(empresa.getLogotipo())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClienteInfo {
        private Long id;
        private String nome;
        private String nuit;
        private String endereco;
        private String telefone;
        private String email;
        private String tipo;
        private BigDecimal descontoPadrao;

        public static ClienteInfo fromEntity(Cliente cliente) {
            if (cliente == null) return null;

            return ClienteInfo.builder()
                    .id(cliente.getId())
                    .nome(cliente.getNome())
                    .nuit(cliente.getNuit())
                    .endereco(cliente.getEndereco())
                    .telefone(cliente.getTelefone())
                    .email(cliente.getEmail())
                    .tipo(cliente.getTipo())
                    .descontoPadrao(cliente.getDescontoPadrao())
                    .build();
        }
    }
}