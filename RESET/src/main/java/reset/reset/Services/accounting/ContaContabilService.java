package reset.reset.Services.accounting;

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
import reset.reset.Models.accounting.ContaContabil;
import reset.reset.Repositories.accounting.ContaContabilRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.util.List;

@Service

@Slf4j
public class ContaContabilService extends BaseServiceImpl<ContaContabil, Long, ContaContabilRepository> {

    private final ContaContabilRepository contaContabilRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public ContaContabilService(ContaContabilRepository repository) {
        super(repository);
        this.contaContabilRepository = repository;
    }

    @Override
    protected void validateBeforeSave(ContaContabil conta) {
        validateEmpresaExists(conta.getEmpresa().getId());
        validateCodigoUniqueness(conta.getCodigo(), conta.getEmpresa().getId(), null);
        validateTipoConta(conta.getTipo());
        validateCodigoFormat(conta.getCodigo());
    }

    @Override
    protected void validateBeforeUpdate(Long id, ContaContabil conta) {
        ContaContabil existing = findByIdOrThrow(id);
        validateEmpresaExists(conta.getEmpresa().getId());
        validateTipoConta(conta.getTipo());
        validateCodigoFormat(conta.getCodigo());

        if (!existing.getCodigo().equals(conta.getCodigo())) {
            validateCodigoUniqueness(conta.getCodigo(), conta.getEmpresa().getId(), id);
        }
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateCodigoUniqueness(String codigo, Long empresaId, Long excludeId) {
        contaContabilRepository.findByEmpresaIdAndCodigo(empresaId, codigo)
                .ifPresent(c -> {
                    if (excludeId == null || !c.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Account code already exists: " + codigo);
                    }
                });
    }

    private void validateTipoConta(String tipo) {
        if (tipo == null || tipo.isEmpty()) {
            throw new BusinessException("Account type is required");
        }

        List<String> tiposValidos = List.of("ATIVO", "PASSIVO", "CUSTO", "RENDIMENTO");
        if (!tiposValidos.contains(tipo)) {
            throw new BusinessException("Invalid account type. Valid types: " + tiposValidos);
        }
    }

    private void validateCodigoFormat(String codigo) {
        if (codigo == null || codigo.isEmpty()) {
            throw new BusinessException("Account code is required");
        }
        if (!codigo.matches("^[0-9.]+$")) {
            throw new BusinessException("Account code must contain only numbers and dots");
        }
    }

    public Page<ContaContabil> findByEmpresaId(Long empresaId, Pageable pageable) {
        return contaContabilRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<ContaContabil> findByEmpresaIdAndTipo(Long empresaId, String tipo) {
        return contaContabilRepository.findByEmpresaIdAndTipo(empresaId, tipo);
    }

    public List<ContaContabil> findAllByEmpresaIdOrderByCodigo(Long empresaId) {
        return contaContabilRepository.findAllByEmpresaIdOrderByCodigo(empresaId);
    }

    public ContaContabil findByEmpresaIdAndCodigo(Long empresaId, String codigo) {
        return contaContabilRepository.findByEmpresaIdAndCodigo(empresaId, codigo)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + codigo));
    }

    @Transactional
    public ContaContabil criarContaPadrao(Long empresaId, String codigo, String descricao, String tipo) {
        ContaContabil conta = new ContaContabil();
        conta.setEmpresa(empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa not found")));
        conta.setCodigo(codigo);
        conta.setDescricao(descricao);
        conta.setTipo(tipo);
        return save(conta);
    }
}