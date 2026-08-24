package reset.reset.Repositories.stock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.stock.Armazem;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArmazemRepository extends BaseRepository<Armazem, Long> {

    List<Armazem> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT a FROM Armazem a WHERE a.empresa.id = :empresaId")
    Page<Armazem> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT a FROM Armazem a WHERE a.empresa.id = :empresaId ORDER BY a.nome ASC")
    List<Armazem> findAllByEmpresaIdOrderByNome(@Param("empresaId") Long empresaId);

   Optional<Armazem> findFirstByEmpresa(Empresa empresa);
}
