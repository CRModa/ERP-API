package reset.reset.Repositories.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.restaurant.Pedido;
import reset.reset.Repositories.BaseRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends BaseRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p WHERE p.empresa.id = :empresaId")
    Page<Pedido> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.mesa.id = :mesaId AND p.status != 'FECHADO' AND p.status != 'CANCELADO'")
    List<Pedido> findPedidosAtivosByMesaId(@Param("mesaId") Long mesaId);

    @Query("SELECT p FROM Pedido p WHERE p.status = :status AND p.empresa.id = :empresaId")
    List<Pedido> findByStatusAndEmpresaId(@Param("status") Pedido.StatusPedido status,
                                          @Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Pedido p WHERE p.status IN ('PENDENTE', 'EM_PREPARO') AND p.empresa.id = :empresaId ORDER BY p.dataPedido ASC")
    List<Pedido> findPedidosEmAndamento(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Pedido p WHERE p.atendente.id = :atendenteId AND p.dataPedido BETWEEN :inicio AND :fim")
    List<Pedido> findByAtendenteAndPeriodo(@Param("atendenteId") Long atendenteId,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim);

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.empresa.id = :empresaId AND p.dataPedido BETWEEN :inicio AND :fim AND p.status = 'FECHADO'")
    BigDecimal sumTotalByEmpresaAndPeriodo(@Param("empresaId") Long empresaId,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.empresa.id = :empresaId AND p.dataPedido BETWEEN :inicio AND :fim AND p.status = 'FECHADO'")
    Long countByEmpresaAndPeriodo(@Param("empresaId") Long empresaId,
                                  @Param("inicio") LocalDateTime inicio,
                                  @Param("fim") LocalDateTime fim);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId ORDER BY p.dataPedido DESC")
    Page<Pedido> findByClienteId(@Param("clienteId") Long clienteId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.mesa.id = :mesaId AND p.status IN ('PENDENTE', 'EM_PREPARO', 'PRONTO')")
    Integer countPedidosAtivosByMesaId(@Param("mesaId") Long mesaId);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.empresa.id = :empresaId AND p.status = :status")
    Long countByEmpresaIdAndStatus(@Param("empresaId") Long empresaId,
                                   @Param("status") Pedido.StatusPedido status);
}
