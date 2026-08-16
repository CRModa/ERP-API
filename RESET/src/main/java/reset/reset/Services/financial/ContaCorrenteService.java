package reset.reset.Services.financial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.customer.Cliente;
import reset.reset.Models.document.Documento;
import reset.reset.Models.financial.ContaCorrente;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.customer.ClienteRepository;
import reset.reset.Repositories.customer.FornecedorRepository;
import reset.reset.Repositories.document.DocumentoRepository;
import reset.reset.Repositories.financial.ContaCorrenteRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.ContaCorrenteFilter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class ContaCorrenteService extends BaseServiceImpl<ContaCorrente, Long, ContaCorrenteRepository> {

    private final ContaCorrenteRepository contaCorrenteRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;
    @Autowired
    private DocumentoRepository documentoRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired

    public ContaCorrenteService(ContaCorrenteRepository repository) {
        super(repository);
        this.contaCorrenteRepository = repository;
    }

    @Override
    protected void validateBeforeSave(ContaCorrente contaCorrente) {
        validateEmpresaExists(contaCorrente.getEmpresa().getId());
        validateClienteOrFornecedor(contaCorrente);
        validateDocumento(contaCorrente);
        validateValor(contaCorrente.getValor());
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateClienteOrFornecedor(ContaCorrente contaCorrente) {
        if (contaCorrente.getCliente() == null && contaCorrente.getFornecedor() == null) {
            throw new BusinessException("Either client or supplier must be specified");
        }

        if (contaCorrente.getCliente() != null) {
            if (!clienteRepository.existsById(contaCorrente.getCliente().getId())) {
                throw new EntityNotFoundException("Client not found with id: " + contaCorrente.getCliente().getId());
            }
        }

        if (contaCorrente.getFornecedor() != null) {
            if (!fornecedorRepository.existsById(contaCorrente.getFornecedor().getId())) {
                throw new EntityNotFoundException("Supplier not found with id: " + contaCorrente.getFornecedor().getId());
            }
        }
    }

    private void validateDocumento(ContaCorrente contaCorrente) {
        if (contaCorrente.getDocumento() != null &&
                !documentoRepository.existsById(contaCorrente.getDocumento().getId())) {
            throw new EntityNotFoundException("Document not found with id: " + contaCorrente.getDocumento().getId());
        }
    }

    private void validateValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Value must be greater than zero");
        }
    }

    @Override
    @Transactional
    public ContaCorrente save(ContaCorrente contaCorrente) {
        // Calculate saldo anterior e atual
        BigDecimal saldoAnterior = calcularSaldoAnterior(contaCorrente);
        contaCorrente.setSaldoAnterior(saldoAnterior);

        BigDecimal saldoAtual;
        if (contaCorrente.getTipoMovimento() == ContaCorrente.TipoMovimento.DEBITO) {
            saldoAtual = saldoAnterior.add(contaCorrente.getValor());
        } else {
            saldoAtual = saldoAnterior.subtract(contaCorrente.getValor());
        }
        contaCorrente.setSaldoAtual(saldoAtual);

        // Set default values
        if (contaCorrente.getDataMovimento() == null) {
            contaCorrente.setDataMovimento(LocalDate.now());
        }
        if (contaCorrente.getPago() == null) {
            contaCorrente.setPago(false);
        }

        ContaCorrente saved = super.save(contaCorrente);

        // Update cliente saldo corrente
        atualizarSaldoCliente(saved);

        return saved;
    }

    private BigDecimal calcularSaldoAnterior(ContaCorrente novaConta) {
        List<ContaCorrente> movimentos;

        if (novaConta.getCliente() != null) {
            movimentos = contaCorrenteRepository.findByClienteIdOrderByDataMovimentoDesc(
                    novaConta.getCliente().getId()
            );
        } else {
            movimentos = contaCorrenteRepository.findByFornecedorIdOrderByDataMovimentoDesc(
                    novaConta.getFornecedor().getId()
            );
        }

        if (movimentos.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return movimentos.get(0).getSaldoAtual();
    }

    private void atualizarSaldoCliente(ContaCorrente conta) {
        if (conta.getCliente() != null) {
            Cliente cliente = clienteRepository.findById(conta.getCliente().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Client not found"));

            List<ContaCorrente> movimentos = contaCorrenteRepository
                    .findByClienteIdOrderByDataMovimentoDesc(cliente.getId());

            if (!movimentos.isEmpty()) {
                cliente.setSaldoCorrente(movimentos.get(0).getSaldoAtual());
                clienteRepository.save(cliente);
            }
        }
    }

    @Transactional
    public ContaCorrente marcarComoPago(Long id) {
        ContaCorrente conta = findByIdOrThrow(id);
        if (conta.getPago()) {
            throw new BusinessException("This movement is already marked as paid");
        }
        conta.setPago(true);
        conta.setDataPagamento(LocalDate.now());
        return contaCorrenteRepository.save(conta);
    }

    @Transactional
    public ContaCorrente criarDebitoCliente(Long clienteId, Long documentoId, BigDecimal valor,
                                            String descricao, LocalDate dataVencimento) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        ContaCorrente conta = new ContaCorrente();
        conta.setEmpresa(cliente.getEmpresa());
        conta.setCliente(cliente);
        conta.setDocumento(documento);
        conta.setTipoMovimento(ContaCorrente.TipoMovimento.DEBITO);
        conta.setValor(valor);
        conta.setDescricao(descricao);
        conta.setDataVencimento(dataVencimento);
        conta.setPago(false);

        return save(conta);
    }

    @Transactional
    public ContaCorrente criarCreditoCliente(Long clienteId, Long documentoId, BigDecimal valor,
                                             String descricao) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        ContaCorrente conta = new ContaCorrente();
        conta.setEmpresa(cliente.getEmpresa());
        conta.setCliente(cliente);
        conta.setDocumento(documento);
        conta.setTipoMovimento(ContaCorrente.TipoMovimento.CREDITO);
        conta.setValor(valor);
        conta.setDescricao(descricao);
        conta.setPago(false);

        return save(conta);
    }

    public List<ContaCorrente> findContasVencidas() {
        return contaCorrenteRepository.findContasVencidas(LocalDate.now());
    }

    public List<ContaCorrente> findDebitosNaoPagosByCliente(Long clienteId) {
        return contaCorrenteRepository.findDebitosNaoPagosByCliente(clienteId);
    }

    public BigDecimal sumDebitosNaoPagosByCliente(Long clienteId) {
        BigDecimal sum = contaCorrenteRepository.sumDebitosNaoPagosByCliente(clienteId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public Page<ContaCorrente> filter(ContaCorrenteFilter filter) {
        return contaCorrenteRepository.filter(filter);
    }
}
