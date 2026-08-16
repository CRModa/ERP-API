package reset.reset.Repositories.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.Tipos.Fatura;
import reset.reset.Repositories.BaseRepository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FaturaRepository extends BaseRepository<Fatura, Long> {

    @Query("SELECT f FROM Fatura f WHERE f.paga = false AND f.vencimento < :data")
    List<Fatura> findFaturasVencidas(@Param("data") LocalDate data);

    @Query("SELECT f FROM Fatura f WHERE f.paga = false")
    List<Fatura> findFaturasNaoPagas();

    @Query("SELECT f FROM Fatura f WHERE f.paga = true")
    List<Fatura> findFaturasPagas();

    @Query("SELECT f FROM Fatura f WHERE f.vencimento BETWEEN :inicio AND :fim")
    List<Fatura> findByVencimentoBetween(@Param("inicio") LocalDate inicio,
                                         @Param("fim") LocalDate fim);

    @Query("SELECT f FROM Fatura f WHERE f.cliente.id = :clienteId AND f.paga = false")
    List<Fatura> findFaturasNaoPagasByCliente(@Param("clienteId") Long clienteId);

    Page<Fatura> findByEmpresaId(Long empresaId, Pageable pageable);
}

