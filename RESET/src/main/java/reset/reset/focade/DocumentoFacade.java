package reset.reset.focade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.Tipos.Fatura;
import reset.reset.Models.document.Tipos.Recibo;
import reset.reset.Models.stock.Stock;
import reset.reset.Services.document.DocumentoService;
import reset.reset.Services.document.FaturaService;
import reset.reset.Services.document.ReciboService;
import reset.reset.Services.financial.ContaCorrenteService;
import reset.reset.Services.product.IvaService;
import reset.reset.Services.stock.StockService;
import reset.reset.dto.CreateFaturaRequest;
import reset.reset.dto.CreateReciboRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoFacade {

    private final DocumentoService documentoService;
    private final FaturaService faturaService;
    private final ReciboService reciboService;
    private final StockService stockService;
    private final ContaCorrenteService contaCorrenteService;
    private final IvaService ivaService;

    @Transactional
    public Fatura criarFaturaComStock(CreateFaturaRequest request) {
        // Validar stock
        for (CreateFaturaRequest.ItemRequest item : request.getItens()) {
            Stock stock = stockService.getStockByProdutoAndArmazem(
                    item.getProdutoId(), request.getArmazemId()
            );

            if (stock.getQuantidadeAtual().compareTo(item.getQuantidade()) < 0) {
                throw new BusinessException(
                        "Insufficient stock for product: " + item.getProdutoId()
                );
            }
        }

        // Criar fatura
        Fatura fatura = new Fatura();
        // ... set campos da fatura

        Fatura savedFatura = faturaService.save(fatura);

        // Baixar stock
        for (CreateFaturaRequest.ItemRequest item : request.getItens()) {
            stockService.removerStock(
                    item.getProdutoId(),
                    request.getArmazemId(),
                    item.getQuantidade(),
                    "Fatura: " + savedFatura.getNumero()
            );
        }

        // Criar lançamento na conta corrente do cliente
        contaCorrenteService.criarDebitoCliente(
                request.getClienteId(),
                savedFatura.getId(),
                savedFatura.getTotal(),
                "Fatura: " + savedFatura.getNumero(),
                savedFatura.getVencimento()
        );

        return savedFatura;
    }

    @Transactional
    public Recibo criarReciboComBaixaFatura(Long faturaId, CreateReciboRequest request) {
        Fatura fatura = faturaService.findByIdOrThrow(faturaId);

        if (fatura.getPaga()) {
            throw new BusinessException("This invoice is already paid");
        }

        // Criar recibo
        Recibo recibo = new Recibo();
        // ... set campos do recibo

        Recibo savedRecibo = reciboService.save(recibo);

        // Marcar fatura como paga
        faturaService.marcarComoPaga(faturaId);

        // Criar crédito na conta corrente
        contaCorrenteService.criarCreditoCliente(
                fatura.getCliente().getId(),
                savedRecibo.getId(),
                request.getValorPago(),
                "Recibo: " + savedRecibo.getNumero()
        );

        return savedRecibo;
    }
}
