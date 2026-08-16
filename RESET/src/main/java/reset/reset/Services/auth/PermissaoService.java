package reset.reset.Services.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Models.auth.Permissao;
import reset.reset.Repositories.auth.PermissaoRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.util.List;

@Service
//@RequiredArgsConstructor
@Slf4j
public class PermissaoService extends BaseServiceImpl<Permissao, Long, PermissaoRepository> {

    private final PermissaoRepository permissaoRepository;

    public PermissaoService(PermissaoRepository repository) {
        super(repository);
        this.permissaoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Permissao permissao) {
        validatePermissionNameUniqueness(permissao.getNome(), null);
    }

    @Override
    protected void validateBeforeUpdate(Long id, Permissao permissao) {
        Permissao existing = findByIdOrThrow(id);
        if (!existing.getNome().equals(permissao.getNome())) {
            validatePermissionNameUniqueness(permissao.getNome(), id);
        }
    }

    private void validatePermissionNameUniqueness(String nome, Long excludeId) {
        permissaoRepository.findByNome(nome)
                .ifPresent(p -> {
                    if (excludeId == null || !p.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Permission already exists: " + nome);
                    }
                });
    }

    public List<Permissao> findByRecurso(String recurso) {
        return permissaoRepository.findByRecurso(recurso);
    }

    public List<Permissao> findByAcao(String acao) {
        return permissaoRepository.findByAcao(acao);
    }

    public List<Permissao> findByRecursoAndAcao(String recurso, String acao) {
        return permissaoRepository.findByRecursoAndAcao(recurso, acao);
    }
}
