package reset.reset.Repositories.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.Tipos.Cotacao;
import reset.reset.Repositories.BaseRepository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CotacaoRepository extends BaseRepository<Cotacao, Long> {

    @Query("SELECT c FROM Cotacao c WHERE c.validoAte < :data AND c.estado = 'ATIVO'")
    List<Cotacao> findCotacoesExpiradas(@Param("data") LocalDate data);

    @Query("SELECT c FROM Cotacao c WHERE c.estado = 'APROVADO'")
    List<Cotacao> findCotacoesAprovadas();

    @Query("SELECT c FROM Cotacao c WHERE c.estado = 'PENDENTE'")
    List<Cotacao> findCotacoesPendentes();

    Page<Cotacao> findByEmpresaId(Long empresaId, Pageable pageable);
}
