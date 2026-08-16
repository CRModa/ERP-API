package reset.reset.Services.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.product.Iva;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.product.IvaRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class IvaService extends BaseServiceImpl<Iva, Long, IvaRepository> {

    private final IvaRepository ivaRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public IvaService(IvaRepository repository) {
        super(repository);
        this.ivaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Iva iva) {
        validateEmpresaExists(iva.getEmpresa().getId());
        validateCodigoUniqueness(iva.getCodigo(), iva.getEmpresa().getId(), null);
        validateTaxa(iva.getTaxa());
    }

    @Override
    protected void validateBeforeUpdate(Long id, Iva iva) {
        Iva existing = findByIdOrThrow(id);
        validateEmpresaExists(iva.getEmpresa().getId());
        validateTaxa(iva.getTaxa());

        if (!existing.getCodigo().equals(iva.getCodigo())) {
            validateCodigoUniqueness(iva.getCodigo(), iva.getEmpresa().getId(), id);
        }
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateCodigoUniqueness(String codigo, Long empresaId, Long excludeId) {
        ivaRepository.findByCodigo(codigo)
                .ifPresent(i -> {
                    if (excludeId == null || !i.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("IVA code already exists: " + codigo);
                    }
                });
    }

    private void validateTaxa(BigDecimal taxa) {
        if (taxa == null || taxa.compareTo(BigDecimal.ZERO) < 0 || taxa.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("IVA rate must be between 0 and 100");
        }
    }

    @Transactional
    public Iva ativarIva(Long id) {
        Iva iva = findByIdOrThrow(id);
        iva.setAtivo(true);
        return ivaRepository.save(iva);
    }

    @Transactional
    public Iva desativarIva(Long id) {
        Iva iva = findByIdOrThrow(id);
        iva.setAtivo(false);
        return ivaRepository.save(iva);
    }

    public Page<Iva> findByEmpresaId(Long empresaId, Pageable pageable) {
        return ivaRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<Iva> findActiveByEmpresaId(Long empresaId) {
        return ivaRepository.findActiveByEmpresaId(empresaId);
    }
}
