package reset.reset.Repositories.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.core.Empresa;
import reset.reset.Repositories.BaseRepository;
import reset.reset.Repositories.specification.EmpresaSpecification;
import reset.reset.Repositories.specification.FilterOperation;
import reset.reset.dto.filter.EmpresaFilter;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends BaseRepository<Empresa, Long> {

    Optional<Empresa> findByNuit(String nuit);

    @Query("SELECT e FROM Empresa e WHERE e.nome LIKE %:nome%")
    List<Empresa> searchByNome(@Param("nome") String nome);

    @Query("SELECT e FROM Empresa e WHERE e.ativo = true")
    Page<Empresa> findActiveEmpresas(Pageable pageable);

    @Query("SELECT COUNT(e) FROM Empresa e WHERE e.ativo = true")
    long countActiveEmpresas();

    default Page<Empresa> filter(EmpresaFilter filter) {
        EmpresaSpecification spec = new EmpresaSpecification();
        spec.addFilter("nome", filter.getNome(), FilterOperation.LIKE);
        spec.addFilter("nuit", filter.getNuit(), FilterOperation.LIKE);
        spec.addFilter("email", filter.getEmail(), FilterOperation.LIKE);
        spec.addFilter("telefone", filter.getTelefone(), FilterOperation.LIKE);
        spec.addFilter("pais", filter.getPais(), FilterOperation.LIKE);
        spec.addDateTimeRange("createdAt", filter.getDataInicio(), filter.getDataFim());
        if (filter.getAtivo() != null) {
            spec.addFilter("ativo", filter.getAtivo(), FilterOperation.EQUALS);
        }
        return findAll(spec, filter.toPageable());
    }
}

