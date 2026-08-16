package reset.reset.Services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.Tipos.Recibo;
import reset.reset.Repositories.document.ReciboRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ReciboService extends BaseServiceImpl<Recibo, Long, ReciboRepository> {

    private final ReciboRepository reciboRepository;

    public ReciboService(ReciboRepository repository) {
        super(repository);
        this.reciboRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Recibo recibo) {
        if (recibo.getFormaPagamento() == null || recibo.getFormaPagamento().isEmpty()) {
            throw new BusinessException("Payment method is required");
        }
        if (recibo.getDataPagamento() == null) {
            recibo.setDataPagamento(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public Recibo save(Recibo recibo) {
        if (recibo.getEstado() == null) {
            recibo.setEstado("PAGO");
        }
        return super.save(recibo);
    }

    public List<Recibo> findByFormaPagamento(String formaPagamento) {
        return reciboRepository.findByFormaPagamento(formaPagamento);
    }

    public List<Recibo> findByReferenciaPagamento(String referencia) {
        return reciboRepository.findByReferenciaPagamento(referencia);
    }

    public List<Recibo> findByDataPagamentoBetween(LocalDateTime inicio, LocalDateTime fim) {
        return reciboRepository.findByDataPagamentoBetween(inicio, fim);
    }

    public Page<Recibo> findByEmpresaId(Long empresaId, Pageable pageable) {
        return reciboRepository.findByEmpresaId(empresaId, pageable);
    }
}
