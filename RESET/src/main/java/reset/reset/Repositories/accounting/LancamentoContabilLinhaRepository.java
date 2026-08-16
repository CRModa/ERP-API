package reset.reset.Repositories.accounting;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.accounting.LancamentoContabilLinha;
import reset.reset.Repositories.BaseRepository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LancamentoContabilLinhaRepository extends BaseRepository<LancamentoContabilLinha, Long> {

    @Query("SELECT l FROM LancamentoContabilLinha l WHERE l.lancamento.id = :lancamentoId")
    List<LancamentoContabilLinha> findByLancamentoId(@Param("lancamentoId") Long lancamentoId);

    @Query("SELECT l FROM LancamentoContabilLinha l WHERE l.contaContabil.id = :contaContabilId")
    List<LancamentoContabilLinha> findByContaContabilId(@Param("contaContabilId") Long contaContabilId);

    @Query("SELECT SUM(l.valor) FROM LancamentoContabilLinha l WHERE l.lancamento.id = :lancamentoId AND l.natureza = 'D'")
    BigDecimal sumDebitosByLancamentoId(@Param("lancamentoId") Long lancamentoId);

    @Query("SELECT SUM(l.valor) FROM LancamentoContabilLinha l WHERE l.lancamento.id = :lancamentoId AND l.natureza = 'C'")
    BigDecimal sumCreditosByLancamentoId(@Param("lancamentoId") Long lancamentoId);
}
