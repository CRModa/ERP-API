package reset.reset.Services.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Models.core.Empresa;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.EmpresaFilter;

import java.util.List;

@Service
@Slf4j
public class EmpresaService extends BaseServiceImpl<Empresa, Long, EmpresaRepository> {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository repository) {
        super(repository);
        this.empresaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Empresa empresa) {
        validateNuitUniqueness(empresa.getNuit(), null);
        validateEmailUniqueness(empresa.getEmail(), null);
        validateTelefoneFormat(empresa.getTelefone());
    }

    @Override
    protected void validateBeforeUpdate(Long id, Empresa empresa) {
        Empresa existing = findByIdOrThrow(id);
        if (!existing.getNuit().equals(empresa.getNuit())) {
            validateNuitUniqueness(empresa.getNuit(), id);
        }
        if (!existing.getEmail().equals(empresa.getEmail())) {
            validateEmailUniqueness(empresa.getEmail(), id);
        }
        validateTelefoneFormat(empresa.getTelefone());
    }

    private void validateNuitUniqueness(String nuit, Long excludeId) {
        empresaRepository.findByNuit(nuit)
                .ifPresent(e -> {
                    if (excludeId == null || !e.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("NUIT already exists: " + nuit);
                    }
                });
    }

    private void validateEmailUniqueness(String email, Long excludeId) {
        if (email != null && !email.isEmpty()) {
            // Check if email exists (we can add email field to Empresa or use custom query)
        }
    }

    private void validateTelefoneFormat(String telefone) {
        if (telefone != null && !telefone.isEmpty()) {
            if (!telefone.matches("^\\+?[0-9\\s-()]{8,20}$")) {
                throw new BusinessException("Invalid phone number format");
            }
        }
    }

    @Transactional
    public Empresa ativarEmpresa(Long id) {
        Empresa empresa = findByIdOrThrow(id);
        empresa.setAtivo(true);
        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa desativarEmpresa(Long id) {
        Empresa empresa = findByIdOrThrow(id);
        empresa.setAtivo(false);
        return empresaRepository.save(empresa);
    }

    public Page<Empresa> filter(EmpresaFilter filter) {
        return empresaRepository.filter(filter);
    }

    public Page<Empresa> findActiveEmpresas(Pageable pageable) {
        return empresaRepository.findActiveEmpresas(pageable);
    }

    public List<Empresa> findAllActive() {
        return empresaRepository.findAllByAtivoTrue();
    }

    public long countActive() {
        return empresaRepository.countActiveEmpresas();
    }
}