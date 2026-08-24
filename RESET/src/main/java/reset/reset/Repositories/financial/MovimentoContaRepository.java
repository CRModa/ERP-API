package reset.reset.Repositories.financial;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.financial.MovimentoConta;
import reset.reset.Repositories.BaseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimentoContaRepository extends BaseRepository<MovimentoConta, Long> {

    @Query("SELECT m FROM MovimentoConta m WHERE m.conta.id = :contaId ORDER BY m.data DESC, m.id DESC")
    Page<MovimentoConta> findByContaId(@Param("contaId") Long contaId, Pageable pageable);

    @Query("SELECT m FROM MovimentoConta m WHERE m.documento.id = :documentoId")
    List<MovimentoConta> findByDocumentoId(@Param("documentoId") Long documentoId);

    @Query("SELECT m FROM MovimentoConta m WHERE m.data BETWEEN :inicio AND :fim ORDER BY m.data DESC")
    List<MovimentoConta> findByDataBetween(@Param("inicio") LocalDate inicio,
                                           @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentoConta m WHERE m.conta.id = :contaId AND m.tipo = :tipo")
    BigDecimal sumByContaIdAndTipo(@Param("contaId") Long contaId,
                                   @Param("tipo") String tipo);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentoConta m " +
            "WHERE m.conta.id = :contaId AND m.tipo = :tipo " +
            "AND m.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumByContaIdAndTipoAndPeriodo(@Param("contaId") Long contaId,
                                             @Param("tipo") String tipo,
                                             @Param("dataInicio") LocalDate dataInicio,
                                             @Param("dataFim") LocalDate dataFim);

    @Query("SELECT m FROM MovimentoConta m WHERE m.conta.id = :contaId ORDER BY m.data DESC, m.id DESC LIMIT 1")
    MovimentoConta findLastMovimentoByContaId(@Param("contaId") Long contaId);

    @Query("SELECT m FROM MovimentoConta m WHERE m.conta.id = :contaId AND m.tipo = :tipo ORDER BY m.data DESC")
    List<MovimentoConta> findByContaIdAndTipo(@Param("contaId") Long contaId,
                                              @Param("tipo") String tipo);

    @Query("SELECT m FROM MovimentoConta m WHERE m.conta.id = :contaId AND m.documento.id = :documentoId")
    List<MovimentoConta> findByContaIdAndDocumentoId(@Param("contaId") Long contaId,
                                                     @Param("documentoId") Long documentoId);

    @Query("SELECT COUNT(m) FROM MovimentoConta m WHERE m.conta.id = :contaId AND m.tipo = :tipo")
    Long countByContaIdAndTipo(@Param("contaId") Long contaId,
                               @Param("tipo") String tipo);

    @Query("SELECT m FROM MovimentoConta m WHERE m.conta.id = :contaId AND m.tipo = :tipo AND m.valor >= :valorMinimo")
    List<MovimentoConta> findByContaIdAndTipoAndValorMinimo(@Param("contaId") Long contaId,
                                                            @Param("tipo") String tipo,
                                                            @Param("valorMinimo") BigDecimal valorMinimo);

    @Query("SELECT m FROM MovimentoConta m WHERE m.conta.id = :contaId " +
            "AND m.data BETWEEN :dataInicio AND :dataFim ORDER BY m.data DESC")
    Page<MovimentoConta> findByContaIdAndPeriodo(@Param("contaId") Long contaId,
                                                 @Param("dataInicio") LocalDate dataInicio,
                                                 @Param("dataFim") LocalDate dataFim,
                                                 Pageable pageable);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentoConta m " +
            "WHERE m.conta.id = :contaId AND m.tipo = :tipo " +
            "AND m.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumByContaIdAndTipoAndDateRange(@Param("contaId") Long contaId,
                                               @Param("tipo") String tipo,
                                               @Param("dataInicio") LocalDate dataInicio,
                                               @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COALESCE(SUM(CASE WHEN m.tipo = 'ENTRADA' THEN m.valor ELSE -m.valor END), 0) " +
            "FROM MovimentoConta m WHERE m.conta.id = :contaId AND m.data <= :data")
    BigDecimal getSaldoEmData(@Param("contaId") Long contaId,
                              @Param("data") LocalDate data);

    @Query("SELECT m FROM MovimentoConta m JOIN m.conta c WHERE c.empresa.id = :empresaId ORDER BY m.data DESC")
    Page<MovimentoConta> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT m FROM MovimentoConta m WHERE m.conta.id = :contaId AND m.data = :data")
    List<MovimentoConta> findByContaIdAndData(@Param("contaId") Long contaId,
                                              @Param("data") LocalDate data);

    @Query("SELECT m.observacao, COUNT(m), SUM(m.valor) FROM MovimentoConta m " +
            "WHERE m.conta.empresa.id = :empresaId AND m.tipo = 'ENTRADA' " +
            "GROUP BY m.observacao")
    List<Object[]> countAndSumByMetodo(@Param("empresaId") Long empresaId);

}
