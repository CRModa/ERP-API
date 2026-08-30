package reset.reset.Services.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Models.auth.User;
import reset.reset.Models.core.Empresa;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.core.EmpresaEstatisticasDTO;
import reset.reset.dto.core.EmpresaResumoDTO;
import reset.reset.dto.filter.EmpresaFilter;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmpresaService extends BaseServiceImpl<Empresa, Long, EmpresaRepository> {

    private final EmpresaRepository empresaRepository;
    @Autowired
    private UserRepository userRepository;

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

    @Transactional
    public Empresa findByUser() {
        return getAuthenticatedUser().getEmpresa();
    }

    // Methods returning full entities (for internal use or full DTO conversion)
    public Page<Empresa> filter(EmpresaFilter filter, Pageable pageable) {
        return empresaRepository.filter(filter, pageable);
    }

    // Methods returning summarized DTOs
    public Page<EmpresaResumoDTO> filterSummarized(EmpresaFilter filter, Pageable pageable) {
        return empresaRepository.filter(filter, pageable)
                .map(EmpresaResumoDTO::fromEntity);
    }

    public Page<EmpresaResumoDTO> findActiveEmpresasSummarized(Pageable pageable) {
        return empresaRepository.findActiveEmpresas(pageable)
                .map(EmpresaResumoDTO::fromEntity);
    }

    public List<EmpresaResumoDTO> findAllSummarized() {
        return empresaRepository.findAll().stream()
                .map(EmpresaResumoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<EmpresaResumoDTO> findAllActiveSummarized() {
        return empresaRepository.findAllByAtivoTrue().stream()
                .map(EmpresaResumoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Statistics
    public EmpresaEstatisticasDTO getStatistics() {
        long total = repository.count();
        long ativas = empresaRepository.countActiveEmpresas();
        return EmpresaEstatisticasDTO.builder()
                .totalEmpresas(total)
                .empresasAtivas(ativas)
                .empresasInativas(total - ativas)
                .build();
    }

    // Legacy methods maintained for backward compatibility
    public Page<Empresa> findActiveEmpresas(Pageable pageable) {
        return empresaRepository.findActiveEmpresas(pageable);
    }

    public List<Empresa> findAllActive() {
        return empresaRepository.findAllByAtivoTrue();
    }

    public long countActive() {
        return empresaRepository.countActiveEmpresas();
    }

    private User getAuthenticatedUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId()).get();
    }
}