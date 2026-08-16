package reset.reset.Repositories.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.Tipos.Recibo;
import reset.reset.Repositories.BaseRepository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReciboRepository extends BaseRepository<Recibo, Long> {

    @Query("SELECT r FROM Recibo r WHERE r.formaPagamento = :formaPagamento")
    List<Recibo> findByFormaPagamento(@Param("formaPagamento") String formaPagamento);

    @Query("SELECT r FROM Recibo r WHERE r.referenciaPagamento = :referencia")
    List<Recibo> findByReferenciaPagamento(@Param("referencia") String referencia);

    @Query("SELECT r FROM Recibo r WHERE r.dataPagamento BETWEEN :inicio AND :fim")
    List<Recibo> findByDataPagamentoBetween(@Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);

    Page<Recibo> findByEmpresaId(Long empresaId, Pageable pageable);
}
