package reset.reset.Repositories.auth;

import org.springframework.stereotype.Repository;
import reset.reset.Models.auth.Permissao;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissaoRepository extends BaseRepository<Permissao, Long> {

    Optional<Permissao> findByNome(String nome);
    List<Permissao> findByRecurso(String recurso);
    List<Permissao> findByAcao(String acao);
    List<Permissao> findByRecursoAndAcao(String recurso, String acao);
    List<Permissao> findByNomeIn(List<String> nomes);
}
