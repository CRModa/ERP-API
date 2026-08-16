package reset.reset.Services.restaurant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.restaurant.Mesa;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.restaurant.MesaRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.util.List;

@Service
@Slf4j
public class MesaService extends BaseServiceImpl<Mesa, Long, MesaRepository> {

    private final MesaRepository mesaRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public MesaService(MesaRepository repository) {
        super(repository);
        this.mesaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Mesa mesa) {
        validateEmpresaExists(mesa.getEmpresa().getId());
        validateNumeroUniqueness(mesa.getEmpresa().getId(), mesa.getNumero(), null);
        validateCapacidade(mesa.getCapacidade());
    }

    @Override
    protected void validateBeforeUpdate(Long id, Mesa mesa) {
        Mesa existing = findByIdOrThrow(id);
        validateEmpresaExists(mesa.getEmpresa().getId());
        validateCapacidade(mesa.getCapacidade());

        if (!existing.getNumero().equals(mesa.getNumero())) {
            validateNumeroUniqueness(mesa.getEmpresa().getId(), mesa.getNumero(), id);
        }
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa não encontrada com ID: " + empresaId);
        }
    }

    private void validateNumeroUniqueness(Long empresaId, String numero, Long excludeId) {
        mesaRepository.findByEmpresaIdAndNumero(empresaId, numero)
                .ifPresent(m -> {
                    if (excludeId == null || !m.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Mesa número " + numero + " já existe");
                    }
                });
    }

    private void validateCapacidade(Integer capacidade) {
        if (capacidade == null || capacidade < 1) {
            throw new BusinessException("Capacidade deve ser pelo menos 1");
        }
    }

    @Transactional
    public Mesa ocuparMesa(Long id) {
        Mesa mesa = findByIdOrThrow(id);
        if (mesa.getStatus() == Mesa.StatusMesa.OCUPADA) {
            throw new BusinessException("Mesa já está ocupada");
        }
        mesa.setStatus(Mesa.StatusMesa.OCUPADA);
        return mesaRepository.save(mesa);
    }

    @Transactional
    public Mesa liberarMesa(Long id) {
        Mesa mesa = findByIdOrThrow(id);
        mesa.setStatus(Mesa.StatusMesa.DISPONIVEL);
        return mesaRepository.save(mesa);
    }

    @Transactional
    public Mesa reservarMesa(Long id) {
        Mesa mesa = findByIdOrThrow(id);
        if (mesa.getStatus() == Mesa.StatusMesa.OCUPADA) {
            throw new BusinessException("Mesa está ocupada, não pode ser reservada");
        }
        mesa.setStatus(Mesa.StatusMesa.RESERVADA);
        return mesaRepository.save(mesa);
    }

    @Transactional
    public Mesa marcarEmLimpeza(Long id) {
        Mesa mesa = findByIdOrThrow(id);
        mesa.setStatus(Mesa.StatusMesa.EM_LIMPEZA);
        return mesaRepository.save(mesa);
    }

    public List<Mesa> findMesasDisponiveis(Long empresaId) {
        return mesaRepository.findMesasDisponiveis(empresaId);
    }

    public Long countMesasOcupadas(Long empresaId) {
        return mesaRepository.countMesasOcupadas(empresaId);
    }

    public Page<Mesa> findActiveByEmpresaId(Long empresaId, Pageable pageable) {
        return mesaRepository.findActiveByEmpresaId(empresaId, pageable);
    }

    public Long countActiveByEmpresaId(Long empresaId) {
        return mesaRepository.countActiveByEmpresaId(empresaId);
    }
}