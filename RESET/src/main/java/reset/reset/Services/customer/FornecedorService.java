package reset.reset.Services.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.User;
import reset.reset.Models.customer.Fornecedor;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.customer.FornecedorRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.BaseFilter;
import reset.reset.dto.filter.FornecedorFilter;

@Service
//@RequiredArgsConstructor
@Slf4j
public class FornecedorService extends BaseServiceImpl<Fornecedor, Long, FornecedorRepository> {

    private final FornecedorRepository fornecedorRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private UserRepository userRepository;

    public FornecedorService(FornecedorRepository repository) {
        super(repository);
        this.fornecedorRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Fornecedor fornecedor) {
//        validateEmpresaExists(fornecedor.getEmpresa().getId());
        if (fornecedor.getNuit() != null && !fornecedor.getNuit().isEmpty()) {
            validateNuitUniqueness(fornecedor.getNuit(), null);
        }
        if (fornecedor.getEmail() != null && !fornecedor.getEmail().isEmpty()) {
            validateEmailUniqueness(fornecedor.getEmail(), null);
        }
        fornecedor.setEmpresa(getAuthenticatedUser().getEmpresa());
    }

    @Override
    protected void validateBeforeUpdate(Long id, Fornecedor fornecedor) {
        Fornecedor existing = findByIdOrThrow(id);
//        validateEmpresaExists(fornecedor.getEmpresa().getId());

        if (fornecedor.getNuit() != null && !fornecedor.getNuit().isEmpty() &&
                !existing.getNuit().equals(fornecedor.getNuit())) {
            validateNuitUniqueness(fornecedor.getNuit(), id);
        }

        if (fornecedor.getEmail() != null && !fornecedor.getEmail().isEmpty() &&
                !existing.getEmail().equals(fornecedor.getEmail())) {
            validateEmailUniqueness(fornecedor.getEmail(), id);
        }
        fornecedor.setEmpresa(getAuthenticatedUser().getEmpresa());
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateNuitUniqueness(String nuit, Long excludeId) {
        fornecedorRepository.findByNuit(nuit)
                .ifPresent(f -> {
                    if (excludeId == null || !f.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("NUIT already exists: " + nuit);
                    }
                });
    }

    private void validateEmailUniqueness(String email, Long excludeId) {
        fornecedorRepository.findByEmail(email)
                .ifPresent(f -> {
                    if (excludeId == null || !f.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Email already exists: " + email);
                    }
                });
    }

    @Transactional
    public Fornecedor ativarFornecedor(Long id) {
        Fornecedor fornecedor = findByIdOrThrow(id);
        fornecedor.setAtivo(true);
        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public Fornecedor desativarFornecedor(Long id) {
        Fornecedor fornecedor = findByIdOrThrow(id);
        fornecedor.setAtivo(false);
        return fornecedorRepository.save(fornecedor);
    }

    public Page<Fornecedor> filter(FornecedorFilter filter) {
        return fornecedorRepository.filter(filter);
    }

    public Page<Fornecedor> findByEmpresaId(Long empresaId, Pageable pageable) {
        return fornecedorRepository.findByEmpresaId(empresaId, pageable);
    }

    public Page<Fornecedor> findActiveByEmpresaId(Long empresaId, Pageable pageable) {
        return fornecedorRepository.findActiveByEmpresaId(empresaId, pageable);
    }

    public long countActiveByEmpresaId(Long empresaId) {
        return fornecedorRepository.countActiveByEmpresaId(empresaId);
    }

    private User getAuthenticatedUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId()).get();
    }
}
