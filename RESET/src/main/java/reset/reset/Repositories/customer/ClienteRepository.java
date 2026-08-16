package reset.reset.Repositories.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.customer.Cliente;
import reset.reset.Repositories.BaseRepository;
import reset.reset.Repositories.specification.ClienteSpecification;
import reset.reset.Repositories.specification.FilterOperation;
import reset.reset.dto.filter.ClienteFilter;
import reset.reset.dto.projection.ClienteResumo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends BaseRepository<Cliente, Long> {

    Optional<Cliente> findByNuit(String nuit);
    Optional<Cliente> findByEmail(String email);
    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId")
    Page<Cliente> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND c.ativo = true")
    Page<Cliente> findActiveByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.tipo = :tipo")
    List<Cliente> findByTipo(@Param("tipo") String tipo);

    @Query("SELECT c FROM Cliente c WHERE c.saldoCorrente > :limite")
    List<Cliente> findWithSaldoMaiorQue(@Param("limite") BigDecimal limite);

    @Query("SELECT c FROM Cliente c WHERE c.limiteCredito < :valor")
    List<Cliente> findWithLimiteCreditoMenorQue(@Param("valor") BigDecimal valor);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.empresa.id = :empresaId AND c.ativo = true")
    long countActiveByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT c.id as id, c.nome as nome, c.nuit as nuit, c.telefone as telefone, " +
            "c.email as email, c.saldoCorrente as saldoCorrente " +
            "FROM Cliente c WHERE c.empresa.id = :empresaId AND c.ativo = true")
    Page<ClienteResumo> findClienteResumoByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    default Page<Cliente> filter(ClienteFilter filter) {
        ClienteSpecification spec = new ClienteSpecification();
        spec.addFilter("nome", filter.getNome(), FilterOperation.LIKE);
        spec.addFilter("nuit", filter.getNuit(), FilterOperation.LIKE);
        spec.addFilter("telefone", filter.getTelefone(), FilterOperation.LIKE);
        spec.addFilter("email", filter.getEmail(), FilterOperation.LIKE);
        spec.addFilter("tipo", filter.getTipo(), FilterOperation.EQUALS);
        spec.addFilter("empresa.id", filter.getEmpresaId(), FilterOperation.EQUALS);
        spec.addBetween("saldoCorrente", filter.getSaldoMinimo(), filter.getSaldoMaximo());
        spec.addBetween("limiteCredito", filter.getLimiteCreditoMinimo(), filter.getLimiteCreditoMaximo());
        spec.addDateTimeRange("dataRegisto", filter.getDataInicio(), filter.getDataFim());
        if (filter.getAtivo() != null) {
            spec.addFilter("ativo", filter.getAtivo(), FilterOperation.EQUALS);
        }
        return findAll(spec, filter.toPageable());
    }
}

