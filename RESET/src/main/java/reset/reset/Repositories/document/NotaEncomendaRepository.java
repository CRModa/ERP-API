package reset.reset.Repositories.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.Tipos.NotaEncomenda;
import reset.reset.Repositories.BaseRepository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NotaEncomendaRepository extends BaseRepository<NotaEncomenda, Long> {

    @Query("SELECT n FROM NotaEncomenda n WHERE n.dataEntregaPrevista < :data AND n.estado != 'ENTREGUE'")
    List<NotaEncomenda> findEncomendasAtrasadas(@Param("data") LocalDate data);

    @Query("SELECT n FROM NotaEncomenda n WHERE n.cotacaoId = :cotacaoId")
    List<NotaEncomenda> findByCotacaoId(@Param("cotacaoId") Long cotacaoId);

    Page<NotaEncomenda> findByEmpresaId(Long empresaId, Pageable pageable);
}
