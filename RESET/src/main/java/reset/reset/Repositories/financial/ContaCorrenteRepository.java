package reset.reset.Repositories.financial;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.financial.ContaCorrente;
import reset.reset.Repositories.BaseRepository;
import reset.reset.Repositories.specification.ContaCorrenteSpecification;
import reset.reset.Repositories.specification.FilterOperation;
import reset.reset.dto.filter.ContaCorrenteFilter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaCorrenteRepository extends BaseRepository<ContaCorrente, Long> {

    @Query("SELECT c FROM ContaCorrente c WHERE c.cliente.id = :clienteId ORDER BY c.dataMovimento DESC")
    List<ContaCorrente> findByClienteIdOrderByDataMovimentoDesc(@Param("clienteId") Long clienteId);

    @Query("SELECT c FROM ContaCorrente c WHERE c.fornecedor.id = :fornecedorId ORDER BY c.dataMovimento DESC")
    List<ContaCorrente> findByFornecedorIdOrderByDataMovimentoDesc(@Param("fornecedorId") Long fornecedorId);

    @Query("SELECT c FROM ContaCorrente c WHERE c.pago = false AND c.dataVencimento < :data")
    List<ContaCorrente> findContasVencidas(@Param("data") LocalDate data);

    @Query("SELECT c FROM ContaCorrente c WHERE c.pago = false AND c.tipoMovimento = 'DEBITO'")
    List<ContaCorrente> findDebitosNaoPagos();

    @Query("SELECT c FROM ContaCorrente c WHERE c.cliente.id = :clienteId AND c.pago = false")
    List<ContaCorrente> findDebitosNaoPagosByCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT c FROM ContaCorrente c WHERE c.empresa.id = :empresaId")
    Page<ContaCorrente> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT SUM(c.valor) FROM ContaCorrente c WHERE c.cliente.id = :clienteId AND c.pago = false AND c.tipoMovimento = 'DEBITO'")
    BigDecimal sumDebitosNaoPagosByCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT SUM(c.valor) FROM ContaCorrente c WHERE c.fornecedor.id = :fornecedorId AND c.pago = false AND c.tipoMovimento = 'CREDITO'")
    BigDecimal sumCreditosNaoPagosByFornecedor(@Param("fornecedorId") Long fornecedorId);

    default Page<ContaCorrente> filter(ContaCorrenteFilter filter) {
        ContaCorrenteSpecification spec = new ContaCorrenteSpecification();
        spec.addFilter("empresa.id", filter.getEmpresaId(), FilterOperation.EQUALS);
        spec.addFilter("cliente.id", filter.getClienteId(), FilterOperation.EQUALS);
        spec.addFilter("fornecedor.id", filter.getFornecedorId(), FilterOperation.EQUALS);
        spec.addFilter("documento.id", filter.getDocumentoId(), FilterOperation.EQUALS);
        spec.addFilter("tipoMovimento", filter.getTipoMovimento(), FilterOperation.EQUALS);
        spec.addBetween("valor", filter.getValorMinimo(), filter.getValorMaximo());
        spec.addDateRange("dataMovimento", filter.getDataMovimentoInicio(), filter.getDataMovimentoFim());
        spec.addDateRange("dataVencimento", filter.getDataVencimentoInicio(), filter.getDataVencimentoFim());
        spec.addDateRange("dataPagamento", filter.getDataPagamentoInicio(), filter.getDataPagamentoFim());
        if (filter.getPago() != null) {
            spec.addFilter("pago", filter.getPago(), FilterOperation.EQUALS);
        }
        return findAll(spec, filter.toPageable());
    }
}

