package reset.reset.Services.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.Tipos.Recibo;
import reset.reset.Repositories.document.ReciboRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.document.ReciboDTO;
import reset.reset.dto.request.ReciboRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    // ==================== MÉTODOS COM RETORNO DTO ====================

    @Transactional
    public ReciboDTO createRecibo(ReciboRequest request) {
        Recibo recibo = request.toEntity();
        Recibo saved = save(recibo);
        return ReciboDTO.fromEntity(saved);
    }

    public ReciboDTO findByIdDTO(Long id) {
        Recibo recibo = findByIdOrThrow(id);
        return ReciboDTO.fromEntity(recibo);
    }

    public Page<ReciboDTO> findAllDTO(Pageable pageable) {
        Page<Recibo> recibos = findAll(pageable);
        return recibos.map(ReciboDTO::fromEntity);
    }

    public Page<ReciboDTO> findByEmpresaIdDTO(Long empresaId, Pageable pageable) {
        Page<Recibo> recibos = reciboRepository.findByEmpresaId(empresaId, pageable);
        return recibos.map(ReciboDTO::fromEntity);
    }

    public List<ReciboDTO> findByFormaPagamentoDTO(String formaPagamento) {
        List<Recibo> recibos = reciboRepository.findByFormaPagamento(formaPagamento);
        return recibos.stream()
                .map(ReciboDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReciboDTO> findByReferenciaPagamentoDTO(String referencia) {
        List<Recibo> recibos = reciboRepository.findByReferenciaPagamento(referencia);
        return recibos.stream()
                .map(ReciboDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReciboDTO> findByDataPagamentoBetweenDTO(LocalDateTime inicio, LocalDateTime fim) {
        List<Recibo> recibos = reciboRepository.findByDataPagamentoBetween(inicio, fim);
        return recibos.stream()
                .map(ReciboDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS ORIGINAIS (MANTIDOS) ====================

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