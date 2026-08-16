package reset.reset.Services.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.customer.Cliente;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.customer.ClienteRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.ClienteFilter;
import reset.reset.dto.projection.ClienteResumo;

import java.math.BigDecimal;
import java.util.List;

@Service
//@RequiredArgsConstructor
@Slf4j
public class ClienteService extends BaseServiceImpl<Cliente, Long, ClienteRepository> {

    private final ClienteRepository clienteRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public ClienteService(ClienteRepository repository) {
        super(repository);
        this.clienteRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Cliente cliente) {
        validateEmpresaExists(cliente.getEmpresa().getId());
        if (cliente.getNuit() != null && !cliente.getNuit().isEmpty()) {
            validateNuitUniqueness(cliente.getNuit(), null);
        }
        if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
            validateEmailUniqueness(cliente.getEmail(), null);
        }
        validateDesconto(cliente.getDescontoPadrao());
        validateLimiteCredito(cliente.getLimiteCredito());
    }

    @Override
    protected void validateBeforeUpdate(Long id, Cliente cliente) {
        Cliente existing = findByIdOrThrow(id);
        validateEmpresaExists(cliente.getEmpresa().getId());

        if (cliente.getNuit() != null && !cliente.getNuit().isEmpty() &&
                !existing.getNuit().equals(cliente.getNuit())) {
            validateNuitUniqueness(cliente.getNuit(), id);
        }

        if (cliente.getEmail() != null && !cliente.getEmail().isEmpty() &&
                !existing.getEmail().equals(cliente.getEmail())) {
            validateEmailUniqueness(cliente.getEmail(), id);
        }
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateNuitUniqueness(String nuit, Long excludeId) {
        clienteRepository.findByNuit(nuit)
                .ifPresent(c -> {
                    if (excludeId == null || !c.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("NUIT already exists: " + nuit);
                    }
                });
    }

    private void validateEmailUniqueness(String email, Long excludeId) {
        clienteRepository.findByEmail(email)
                .ifPresent(c -> {
                    if (excludeId == null || !c.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Email already exists: " + email);
                    }
                });
    }

    private void validateDesconto(BigDecimal desconto) {
        if (desconto != null && (desconto.compareTo(BigDecimal.ZERO) < 0 || desconto.compareTo(new BigDecimal("100")) > 0)) {
            throw new BusinessException("Discount must be between 0 and 100");
        }
    }

    private void validateLimiteCredito(BigDecimal limite) {
        if (limite != null && limite.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Credit limit cannot be negative");
        }
    }

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        // Initialize default values
        if (cliente.getDescontoPadrao() == null) {
            cliente.setDescontoPadrao(BigDecimal.ZERO);
        }
        if (cliente.getLimiteCredito() == null) {
            cliente.setLimiteCredito(BigDecimal.ZERO);
        }
        if (cliente.getSaldoCorrente() == null) {
            cliente.setSaldoCorrente(BigDecimal.ZERO);
        }
        return super.save(cliente);
    }

    @Transactional
    public Cliente atualizarSaldo(Long clienteId, BigDecimal valor) {
        Cliente cliente = findByIdOrThrow(clienteId);
        BigDecimal novoSaldo = cliente.getSaldoCorrente().add(valor);

        // Validate credit limit
        if (novoSaldo.compareTo(cliente.getLimiteCredito()) > 0) {
            throw new BusinessException("Credit limit exceeded. Current limit: " + cliente.getLimiteCredito());
        }

        cliente.setSaldoCorrente(novoSaldo);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente ativarCliente(Long id) {
        Cliente cliente = findByIdOrThrow(id);
        cliente.setAtivo(true);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente desativarCliente(Long id) {
        Cliente cliente = findByIdOrThrow(id);
        cliente.setAtivo(false);
        return clienteRepository.save(cliente);
    }

    public Page<Cliente> filter(ClienteFilter filter) {
        return clienteRepository.filter(filter);
    }

    public Page<Cliente> findByEmpresaId(Long empresaId, Pageable pageable) {
        return clienteRepository.findByEmpresaId(empresaId, pageable);
    }

    public Page<Cliente> findActiveByEmpresaId(Long empresaId, Pageable pageable) {
        return clienteRepository.findActiveByEmpresaId(empresaId, pageable);
    }

    public Page<ClienteResumo> findClienteResumoByEmpresaId(Long empresaId, Pageable pageable) {
        return clienteRepository.findClienteResumoByEmpresaId(empresaId, pageable);
    }

    public List<Cliente> findByTipo(String tipo) {
        return clienteRepository.findByTipo(tipo);
    }

    public List<Cliente> findWithSaldoMaiorQue(BigDecimal valor) {
        return clienteRepository.findWithSaldoMaiorQue(valor);
    }

    public long countActiveByEmpresaId(Long empresaId) {
        return clienteRepository.countActiveByEmpresaId(empresaId);
    }
}
