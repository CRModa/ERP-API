package reset.reset.Repositories.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.customer.Fornecedor;
import reset.reset.Repositories.BaseRepository;
import reset.reset.Repositories.specification.FilterOperation;
import reset.reset.Repositories.specification.FornecedorSpecification;
import reset.reset.dto.filter.FornecedorFilter;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorRepository extends BaseRepository<Fornecedor, Long> {

    Optional<Fornecedor> findByNuit(String nuit);
    Optional<Fornecedor> findByEmail(String email);
    List<Fornecedor> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT f FROM Fornecedor f WHERE f.empresa.id = :empresaId")
    Page<Fornecedor> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT f FROM Fornecedor f WHERE f.empresa.id = :empresaId AND f.ativo = true")
    Page<Fornecedor> findActiveByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Fornecedor f WHERE f.empresa.id = :empresaId AND f.ativo = true")
    long countActiveByEmpresaId(@Param("empresaId") Long empresaId);

    default Page<Fornecedor> filter(FornecedorFilter filter) {
        FornecedorSpecification spec = new FornecedorSpecification();
        spec.addFilter("nome", filter.getNome(), FilterOperation.LIKE);
        spec.addFilter("nuit", filter.getNuit(), FilterOperation.LIKE);
        spec.addFilter("email", filter.getEmail(), FilterOperation.LIKE);
        spec.addFilter("telefone", filter.getTelefone(), FilterOperation.LIKE);
        spec.addFilter("empresa.id", filter.getEmpresaId(), FilterOperation.EQUALS);
//        if (filter.getAtivo() != null) {
//            spec.addFilter("ativo", filter.getAtivo(), FilterOperation.EQUALS);
//        }
        return findAll(spec, filter.toPageable());
    }
}

