package reset.reset.Repositories.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.product.Desconto;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DescontoRepository extends BaseRepository<Desconto, Long> {

    List<Desconto> findByTipo(String tipo);

    @Query("SELECT d FROM Desconto d WHERE d.empresa.id = :empresaId")
    Page<Desconto> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT d FROM Desconto d WHERE d.empresa.id = :empresaId AND d.tipo = :tipo")
    List<Desconto> findByEmpresaIdAndTipo(@Param("empresaId") Long empresaId, @Param("tipo") String tipo);

    @Query("SELECT d FROM Desconto d WHERE d.empresa.id = :empresaId AND d.ativo = true")
    List<Desconto> findActiveByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT d FROM Desconto d WHERE d.descricao LIKE %:descricao% AND d.empresa.id = :empresaId")
    List<Desconto> searchByDescricaoAndEmpresa(@Param("descricao") String descricao,
                                               @Param("empresaId") Long empresaId);

    Optional<Desconto> findByDescricaoAndEmpresaId(String descricao, Long empresaId);

    @Query("SELECT COUNT(d) FROM Desconto d WHERE d.empresa.id = :empresaId")
    long countByEmpresaId(@Param("empresaId") Long empresaId);
}
