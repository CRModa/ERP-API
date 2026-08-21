package reset.reset.Repositories.purchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.purchase.Compra;
import reset.reset.Repositories.BaseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompraRepository extends BaseRepository<Compra, Long> {

    @Query("SELECT c FROM Compra c WHERE c.fornecedor.id = :fornecedorId")
    Page<Compra> findByFornecedorId(@Param("fornecedorId") Long fornecedorId, Pageable pageable);

    @Query("SELECT c FROM Compra c WHERE c.estado = :estado")
    List<Compra> findByEstado(@Param("estado") String estado);

    @Query("SELECT c FROM Compra c WHERE c.data BETWEEN :dataInicio AND :dataFim")
    List<Compra> findByDataBetween(@Param("dataInicio") LocalDate dataInicio,
                                   @Param("dataFim") LocalDate dataFim);

    @Query("SELECT SUM(c.total) FROM Compra c WHERE c.empresa.id = :empresaId AND c.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumTotalByEmpresaAndPeriodo(@Param("empresaId") Long empresaId,
                                           @Param("dataInicio") LocalDate dataInicio,
                                           @Param("dataFim") LocalDate dataFim);

    @Query("SELECT c FROM Compra c WHERE c.estado = :estado")
    Page<Compra> findByEstadoPageable(@Param("estado") String estado, Pageable pageable);

    Page<Compra> findByEmpresaId(Long empresaId, Pageable pageable);
}