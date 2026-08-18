package reset.reset.Repositories.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.auth.User;
import reset.reset.Repositories.BaseRepository;
import reset.reset.Repositories.specification.FilterOperation;
import reset.reset.Repositories.specification.UserSpecification;
import reset.reset.dto.filter.UserFilter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.roles r
    LEFT JOIN FETCH r.permissoes
    LEFT JOIN FETCH u.permissoes
    WHERE u.username = :username
    """)
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.empresa.id = :empresaId")
    List<User> findByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT u FROM User u WHERE u.empresa.id = :empresaId AND u.ativo = true")
    Page<User> findActiveByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.perfil = :perfil")
    List<User> findByPerfil(@Param("perfil") String perfil);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.nome = :roleName")
    List<User> findByRole(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.ultimoLogin < :data AND u.ativo = true")
    List<User> findInactiveUsers(@Param("data") LocalDateTime data);

    default Page<User> filter(UserFilter filter, Pageable pageable) {
        UserSpecification spec = buildSpecification(filter);
        return findAll(spec, pageable);
    }

    default UserSpecification buildSpecification(UserFilter filter) {
        UserSpecification spec = new UserSpecification();

        // Filtros básicos
        spec.addFilter("username", filter.getUsername(), FilterOperation.LIKE);
        spec.addFilter("nome", filter.getNome(), FilterOperation.LIKE);
        spec.addFilter("email", filter.getEmail(), FilterOperation.LIKE);
        spec.addFilter("perfil", filter.getPerfil(), FilterOperation.LIKE);
        spec.addFilter("empresa.id", filter.getEmpresaId(), FilterOperation.EQUALS);

        // Filtro de data
        spec.addDateTimeRange("dataRegisto", filter.getDataInicio(), filter.getDataFim());

        // Filtro booleano
        if (filter.getAtivo() != null) {
            spec.addFilter("ativo", filter.getAtivo(), FilterOperation.EQUALS);
        }

        return spec;
    }
}

