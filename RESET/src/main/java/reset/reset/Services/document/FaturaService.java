package reset.reset.Services.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.Tipos.Fatura;
import reset.reset.Repositories.document.FaturaRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.document.FaturaDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FaturaService extends BaseServiceImpl<Fatura, Long, FaturaRepository> {

    private final FaturaRepository faturaRepository;

    public FaturaService(FaturaRepository repository) {
        super(repository);
        this.faturaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Fatura fatura) {
        if (fatura.getVencimento() != null && fatura.getVencimento().isBefore(fatura.getData())) {
            throw new BusinessException("Due date cannot be before document date");
        }
    }

    // ==================== MÉTODOS COM RETORNO DTO ====================

    public FaturaDTO findByIdDTO(Long id) {
        Fatura fatura = findByIdOrThrow(id);
        return FaturaDTO.fromEntity(fatura);
    }

    public Page<FaturaDTO> findAllDTO(Pageable pageable) {
        Page<Fatura> faturas = findAll(pageable);
        return faturas.map(FaturaDTO::fromEntity);
    }

    public Page<FaturaDTO> findByEmpresaIdDTO(Long empresaId, Pageable pageable) {
        Page<Fatura> faturas = faturaRepository.findByEmpresaId(empresaId, pageable);
        return faturas.map(FaturaDTO::fromEntity);
    }

    public List<FaturaDTO> findFaturasNaoPagasDTO() {
        List<Fatura> faturas = faturaRepository.findFaturasNaoPagas();
        return faturas.stream()
                .map(FaturaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FaturaDTO> findFaturasVencidasDTO() {
        List<Fatura> faturas = faturaRepository.findFaturasVencidas(LocalDate.now());
        return faturas.stream()
                .map(FaturaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FaturaDTO> findFaturasNaoPagasByClienteDTO(Long clienteId) {
        List<Fatura> faturas = faturaRepository.findFaturasNaoPagasByCliente(clienteId);
        return faturas.stream()
                .map(FaturaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public FaturaDTO marcarComoPagaDTO(Long id) {
        Fatura fatura = marcarComoPaga(id);
        return FaturaDTO.fromEntity(fatura);
    }

    @Transactional
    public FaturaDTO marcarComoNaoPagaDTO(Long id) {
        Fatura fatura = marcarComoNaoPaga(id);
        return FaturaDTO.fromEntity(fatura);
    }

    // ==================== MÉTODOS ORIGINAIS (MANTIDOS) ====================

    @Transactional
    public Fatura marcarComoPaga(Long id) {
        Fatura fatura = findByIdOrThrow(id);
        fatura.setPaga(true);
        return faturaRepository.save(fatura);
    }

    @Transactional
    public Fatura marcarComoNaoPaga(Long id) {
        Fatura fatura = findByIdOrThrow(id);
        fatura.setPaga(false);
        return faturaRepository.save(fatura);
    }

    public List<Fatura> findFaturasVencidas() {
        return faturaRepository.findFaturasVencidas(LocalDate.now());
    }

    public List<Fatura> findFaturasNaoPagas() {
        return faturaRepository.findFaturasNaoPagas();
    }

    public List<Fatura> findFaturasNaoPagasByCliente(Long clienteId) {
        return faturaRepository.findFaturasNaoPagasByCliente(clienteId);
    }

    public Page<Fatura> findByEmpresaId(Long empresaId, Pageable pageable) {
        return faturaRepository.findByEmpresaId(empresaId, pageable);
    }
}