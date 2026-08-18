package reset.reset.Services.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.Role;
import reset.reset.Models.auth.User;
import reset.reset.Repositories.auth.RoleRepository;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.auth.ChangePasswordRequest;
import reset.reset.dto.auth.UserResumoDTO;
import reset.reset.dto.filter.UserFilter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService extends BaseServiceImpl<User, Long, UserRepository> {

    private final UserRepository utilizadorRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository) {
        super(repository);
        this.utilizadorRepository = repository;
    }

    @Override
    protected void validateBeforeSave(User utilizador) {
        validateUsernameUniqueness(utilizador.getUsername(), null);
        if (utilizador.getEmail() != null && !utilizador.getEmail().isEmpty()) {
            validateEmailUniqueness(utilizador.getEmail(), null);
        }
        validatePassword(utilizador.getPassword());
    }

    @Override
    protected void validateBeforeUpdate(Long id, User utilizador) {
        User existing = findByIdOrThrow(id);
        if (!existing.getUsername().equals(utilizador.getUsername())) {
            validateUsernameUniqueness(utilizador.getUsername(), id);
        }
        if (utilizador.getEmail() != null && !utilizador.getEmail().isEmpty() &&
                !existing.getEmail().equals(utilizador.getEmail())) {
            validateEmailUniqueness(utilizador.getEmail(), id);
        }
    }

    private void validateUsernameUniqueness(String username, Long excludeId) {
        utilizadorRepository.findByUsername(username)
                .ifPresent(u -> {
                    if (excludeId == null || !u.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Username already exists: " + username);
                    }
                });
    }

    private void validateEmailUniqueness(String email, Long excludeId) {
        utilizadorRepository.findByEmail(email)
                .ifPresent(u -> {
                    if (excludeId == null || !u.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Email already exists: " + email);
                    }
                });
    }

    private void validatePassword(String password) {
        if (password != null && password.length() < 6) {
            throw new BusinessException("Password must be at least 6 characters");
        }
    }

    @Override
    @Transactional
    public User save(User utilizador) {
        if (utilizador.getPassword() != null && !utilizador.getPassword().isEmpty()) {
            utilizador.setPassword(passwordEncoder.encode(utilizador.getPassword()));
        }
        return super.save(utilizador);
    }

    @Transactional
    public User changePassword(Long userId, ChangePasswordRequest request) {
        User utilizador = findByIdOrThrow(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), utilizador.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("New password and confirm password do not match");
        }

        validatePassword(request.getNewPassword());
        utilizador.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return utilizadorRepository.save(utilizador);
    }

    @Transactional
    public User resetPassword(Long userId, String newPassword) {
        User utilizador = findByIdOrThrow(userId);
        validatePassword(newPassword);
        utilizador.setPassword(passwordEncoder.encode(newPassword));
        return utilizadorRepository.save(utilizador);
    }

    @Transactional
    public User updateRoles(Long userId, Set<String> roleNames) {
        User utilizador = findByIdOrThrow(userId);
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByNome(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        utilizador.setRoles(roles);
        return utilizadorRepository.save(utilizador);
    }

    @Transactional
    public User ativarUser(Long id) {
        User utilizador = findByIdOrThrow(id);
        utilizador.setAtivo(true);
        return utilizadorRepository.save(utilizador);
    }

    @Transactional
    public User desativarUser(Long id) {
        User utilizador = findByIdOrThrow(id);
        utilizador.setAtivo(false);
        return utilizadorRepository.save(utilizador);
    }

    @Transactional
    public User updateLastLogin(Long id) {
        User utilizador = findByIdOrThrow(id);
        utilizador.setUltimoLogin(LocalDateTime.now());
        return utilizadorRepository.save(utilizador);
    }

    // Methods returning summarized DTOs (for lists and pages)
    public Page<UserResumoDTO> filterSummarized(UserFilter filter, Pageable pageable) {
        return utilizadorRepository.filter(filter, pageable)
                .map(UserResumoDTO::fromEntity);
    }

    public List<UserResumoDTO> findAllSummarized() {
        return utilizadorRepository.findAll().stream()
                .map(UserResumoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<UserResumoDTO> findActiveByEmpresaIdSummarized(Long empresaId, Pageable pageable) {
        return utilizadorRepository.findActiveByEmpresaId(empresaId, pageable)
                .map(UserResumoDTO::fromEntity);
    }

    public List<UserResumoDTO> findByRoleSummarized(String roleName) {
        return utilizadorRepository.findByRole(roleName).stream()
                .map(UserResumoDTO::fromEntity)
                .collect(Collectors.toList());
    }


    public Page<User> findActiveByEmpresaId(Long empresaId, Pageable pageable) {
        return utilizadorRepository.findActiveByEmpresaId(empresaId, pageable);
    }

    public List<User> findByRole(String roleName) {
        return utilizadorRepository.findByRole(roleName);
    }

    public List<User> findInactiveUsers(LocalDateTime data) {
        return utilizadorRepository.findInactiveUsers(data);
    }

    public boolean existsByUsername(String username) {
        return utilizadorRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return utilizadorRepository.existsByEmail(email);
    }
}