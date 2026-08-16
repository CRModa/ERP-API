package reset.reset.Repositories.accounting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.accounting.LancamentoContabil;
import reset.reset.Repositories.BaseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LancamentoContabilRepository extends BaseRepository<LancamentoContabil, Long> {

    @Query("SELECT l FROM LancamentoContabil l WHERE l.empresa.id = :empresaId")
    Page<LancamentoContabil> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT l FROM LancamentoContabil l WHERE l.diario.id = :diarioId")
    List<LancamentoContabil> findByDiarioId(@Param("diarioId") Long diarioId);

    @Query("SELECT l FROM LancamentoContabil l WHERE l.documento.id = :documentoId")
    List<LancamentoContabil> findByDocumentoId(@Param("documentoId") Long documentoId);

    @Query("SELECT l FROM LancamentoContabil l WHERE l.dataLancamento BETWEEN :dataInicio AND :dataFim")
    List<LancamentoContabil> findByDataLancamentoBetween(@Param("dataInicio") LocalDate dataInicio,
                                                         @Param("dataFim") LocalDate dataFim);

    @Query("SELECT l FROM LancamentoContabil l WHERE l.numeroLancamento = :numeroLancamento")
    Optional<LancamentoContabil> findByNumeroLancamento(@Param("numeroLancamento") String numeroLancamento);

    @Query("SELECT MAX(l.numeroLancamento) FROM LancamentoContabil l WHERE l.empresa.id = :empresaId")
    String findMaxNumeroLancamentoByEmpresaId(@Param("empresaId") Long empresaId);
}

