package reset.reset.Repositories.auth;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.auth.Role;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends BaseRepository<Role, Long> {

    Optional<Role> findByNome(String nome);
    List<Role> findByAtivoTrue();
    boolean existsByNome(String nome);

    @Query("SELECT r FROM Role r WHERE r.nome IN :nomes")
    List<Role> findByNomesIn(@Param("nomes") List<String> nomes);
}