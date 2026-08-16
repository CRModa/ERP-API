package reset.reset.Repositories.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.product.Iva;
import reset.reset.Repositories.BaseRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IvaRepository extends BaseRepository<Iva, Long> {

    Optional<Iva> findByCodigo(String codigo);
    List<Iva> findByTaxa(BigDecimal taxa);

    @Query("SELECT i FROM Iva i WHERE i.empresa.id = :empresaId")
    Page<Iva> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT i FROM Iva i WHERE i.empresa.id = :empresaId AND i.ativo = true")
    List<Iva> findActiveByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT i FROM Iva i WHERE i.empresa.id = :empresaId ORDER BY i.taxa ASC")
    List<Iva> findAllByEmpresaIdOrderByTaxa(@Param("empresaId") Long empresaId);
}