package reset.reset.Repositories.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.Documento;
import reset.reset.Repositories.BaseRepository;
import reset.reset.Repositories.specification.DocumentoSpecification;
import reset.reset.Repositories.specification.FilterOperation;
import reset.reset.dto.filter.DocumentoFilter;
import reset.reset.dto.projection.DocumentoResumo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoRepository extends BaseRepository<Documento, Long> {

    Optional<Documento> findByNumero(String numero);

    @Query("SELECT d FROM Documento d WHERE d.empresa.id = :empresaId")
    Page<Documento> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT d FROM Documento d WHERE d.cliente.id = :clienteId")
    Page<Documento> findByClienteId(@Param("clienteId") Long clienteId, Pageable pageable);

    @Query("SELECT d FROM Documento d WHERE d.tipo.id = :tipoId")
    Page<Documento> findByTipoId(@Param("tipoId") Long tipoId, Pageable pageable);

    @Query("SELECT d FROM Documento d WHERE d.estado = :estado")
    Page<Documento> findByEstado(@Param("estado") String estado, Pageable pageable);

    @Query("SELECT d FROM Documento d WHERE d.data BETWEEN :dataInicio AND :dataFim")
    List<Documento> findByDataBetween(@Param("dataInicio") LocalDate dataInicio,
                                      @Param("dataFim") LocalDate dataFim);

    @Query("SELECT d FROM Documento d WHERE d.total BETWEEN :minTotal AND :maxTotal")
    List<Documento> findByTotalBetween(@Param("minTotal") BigDecimal minTotal,
                                       @Param("maxTotal") BigDecimal maxTotal);

    @Query("SELECT d FROM Documento d WHERE d.cliente.id = :clienteId AND d.estado = :estado")
    Page<Documento> findByClienteIdAndEstado(@Param("clienteId") Long clienteId,
                                             @Param("estado") String estado,
                                             Pageable pageable);

    @Query("SELECT SUM(d.total) FROM Documento d WHERE d.empresa.id = :empresaId AND d.data BETWEEN :dataInicio AND :dataFim")
    BigDecimal sumTotalByEmpresaAndPeriodo(@Param("empresaId") Long empresaId,
                                           @Param("dataInicio") LocalDate dataInicio,
                                           @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COUNT(d) FROM Documento d WHERE d.empresa.id = :empresaId AND d.data BETWEEN :dataInicio AND :dataFim")
    long countByEmpresaAndPeriodo(@Param("empresaId") Long empresaId,
                                  @Param("dataInicio") LocalDate dataInicio,
                                  @Param("dataFim") LocalDate dataFim);

    @Query("SELECT d.id as id, d.numero as numero, d.data as data, d.total as total, " +
            "d.estado as estado, c.nome as clienteNome, t.descricao as tipoDescricao, SIZE(d.itens) as quantidadeItens " +
            "FROM Documento d LEFT JOIN d.cliente c JOIN d.tipo t " +
            "WHERE d.empresa.id = :empresaId")
    Page<DocumentoResumo> findDocumentoResumoByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    default Page<Documento> filter(DocumentoFilter filter) {
        DocumentoSpecification spec = new DocumentoSpecification();
        spec.addFilter("numero", filter.getNumero(), FilterOperation.LIKE);
        spec.addFilter("empresa.id", filter.getEmpresaId(), FilterOperation.EQUALS);
        spec.addFilter("cliente.id", filter.getClienteId(), FilterOperation.EQUALS);
        spec.addFilter("tipo.id", filter.getTipoId(), FilterOperation.EQUALS);
        spec.addFilter("estado", filter.getEstado(), FilterOperation.EQUALS);
        spec.addBetween("total", filter.getTotalMinimo(), filter.getTotalMaximo());
        spec.addDateRange("data", filter.getDataInicio(), filter.getDataFim());
        spec.addDateTimeRange("dataRegisto", filter.getDataRegistoInicio(), filter.getDataRegistoFim());
        return findAll(spec, filter.toPageable());
    }
}

