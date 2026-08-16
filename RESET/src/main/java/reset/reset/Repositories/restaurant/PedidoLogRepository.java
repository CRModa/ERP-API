package reset.reset.Repositories.restaurant;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.restaurant.PedidoLog;
import reset.reset.Repositories.BaseRepository;

import java.util.List;

@Repository
public interface PedidoLogRepository extends BaseRepository<PedidoLog, Long> {

    @Query("SELECT l FROM PedidoLog l WHERE l.pedido.id = :pedidoId ORDER BY l.createdAt DESC")
    List<PedidoLog> findByPedidoIdOrderByCreatedAtDesc(@Param("pedidoId") Long pedidoId);

    @Query("SELECT l FROM PedidoLog l WHERE l.utilizador.id = :utilizadorId ORDER BY l.createdAt DESC")
    List<PedidoLog> findByUtilizadorId(@Param("utilizadorId") Long utilizadorId);
}
