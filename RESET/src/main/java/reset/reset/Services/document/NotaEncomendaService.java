package reset.reset.Services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.Tipos.NotaEncomenda;
import reset.reset.Repositories.document.NotaEncomendaRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class NotaEncomendaService extends BaseServiceImpl<NotaEncomenda, Long, NotaEncomendaRepository> {

    private final NotaEncomendaRepository notaEncomendaRepository;

    public NotaEncomendaService(NotaEncomendaRepository repository) {
        super(repository);
        this.notaEncomendaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(NotaEncomenda notaEncomenda) {
        if (notaEncomenda.getCotacaoId() == null) {
            throw new BusinessException("Quote ID is required");
        }
        if (notaEncomenda.getDataEntregaPrevista() != null &&
                notaEncomenda.getDataEntregaPrevista().isBefore(notaEncomenda.getData())) {
            throw new BusinessException("Expected delivery date cannot be before document date");
        }
        if (notaEncomenda.getPortes() != null && notaEncomenda.getPortes().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Shipping cost cannot be negative");
        }
    }

    @Override
    @Transactional
    public NotaEncomenda save(NotaEncomenda notaEncomenda) {
        if (notaEncomenda.getPortes() == null) {
            notaEncomenda.setPortes(BigDecimal.ZERO);
        }
        if (notaEncomenda.getEstado() == null) {
            notaEncomenda.setEstado("PENDENTE");
        }
        return super.save(notaEncomenda);
    }

    @Transactional
    public NotaEncomenda enviarParaProcessamento(Long id) {
        NotaEncomenda nota = findByIdOrThrow(id);

        if (!"PENDENTE".equals(nota.getEstado())) {
            throw new BusinessException("Only pending orders can be sent for processing");
        }

        nota.setEstado("EM_PROCESSAMENTO");
        return notaEncomendaRepository.save(nota);
    }

    @Transactional
    public NotaEncomenda marcarComoEnviado(Long id) {
        NotaEncomenda nota = findByIdOrThrow(id);

        if (!"EM_PROCESSAMENTO".equals(nota.getEstado())) {
            throw new BusinessException("Only orders in processing can be marked as shipped");
        }

        nota.setEstado("ENVIADO");
        return notaEncomendaRepository.save(nota);
    }

    @Transactional
    public NotaEncomenda marcarComoEntregue(Long id) {
        NotaEncomenda nota = findByIdOrThrow(id);

        if (!"ENVIADO".equals(nota.getEstado())) {
            throw new BusinessException("Only shipped orders can be marked as delivered");
        }

        nota.setEstado("ENTREGUE");
        return notaEncomendaRepository.save(nota);
    }

    public List<NotaEncomenda> findEncomendasAtrasadas() {
        return notaEncomendaRepository.findEncomendasAtrasadas(LocalDate.now());
    }

    public List<NotaEncomenda> findByCotacaoId(Long cotacaoId) {
        return notaEncomendaRepository.findByCotacaoId(cotacaoId);
    }

    public Page<NotaEncomenda> findByEmpresaId(Long empresaId, Pageable pageable) {
        return notaEncomendaRepository.findByEmpresaId(empresaId, pageable);
    }
}