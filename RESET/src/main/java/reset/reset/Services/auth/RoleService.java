package reset.reset.Services.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.Permissao;
import reset.reset.Models.auth.Role;
import reset.reset.Repositories.auth.PermissaoRepository;
import reset.reset.Repositories.auth.RoleRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
@Slf4j
public class RoleService extends BaseServiceImpl<Role, Long, RoleRepository> {

    private final RoleRepository roleRepository;
    @Autowired
    private PermissaoRepository permissaoRepository;

    public RoleService(RoleRepository repository) {
        super(repository);
        this.roleRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Role role) {
        validateRoleNameUniqueness(role.getNome(), null);
    }

    @Override
    protected void validateBeforeUpdate(Long id, Role role) {
        Role existing = findByIdOrThrow(id);
        if (!existing.getNome().equals(role.getNome())) {
            validateRoleNameUniqueness(role.getNome(), id);
        }
    }

    private void validateRoleNameUniqueness(String nome, Long excludeId) {
        roleRepository.findByNome(nome)
                .ifPresent(r -> {
                    if (excludeId == null || !r.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Role already exists: " + nome);
                    }
                });
    }

    @Override
    @Transactional
    public Role save(Role role) {
        // Ensure permissions exist
        if (role.getPermissoes() != null) {
            Set<Permissao> validPermissions = new HashSet<>();
            for (Permissao p : role.getPermissoes()) {
                Permissao permissao = permissaoRepository.findByNome(p.getNome())
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + p.getNome()));
                validPermissions.add(permissao);
            }
            role.setPermissoes(validPermissions);
        }
        return super.save(role);
    }

    @Transactional
    public Role addPermissions(Long roleId, Set<String> permissionNames) {
        Role role = findByIdOrThrow(roleId);
        Set<Permissao> permissoes = new HashSet<>(role.getPermissoes());

        for (String permName : permissionNames) {
            Permissao permissao = permissaoRepository.findByNome(permName)
                    .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + permName));
            permissoes.add(permissao);
        }

        role.setPermissoes(permissoes);
        return roleRepository.save(role);
    }

    @Transactional
    public Role removePermissions(Long roleId, Set<String> permissionNames) {
        Role role = findByIdOrThrow(roleId);
        Set<Permissao> permissoes = role.getPermissoes().stream()
                .filter(p -> !permissionNames.contains(p.getNome()))
                .collect(Collectors.toSet());

        role.setPermissoes(permissoes);
        return roleRepository.save(role);
    }

    @Transactional
    public Role ativarRole(Long id) {
        Role role = findByIdOrThrow(id);
        role.setAtivo(true);
        return roleRepository.save(role);
    }

    @Transactional
    public Role desativarRole(Long id) {
        Role role = findByIdOrThrow(id);
        role.setAtivo(false);
        return roleRepository.save(role);
    }

    public List<Role> findActiveRoles() {
        return roleRepository.findByAtivoTrue();
    }
}
