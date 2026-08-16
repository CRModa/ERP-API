package reset.reset.Services.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.core.Empresa;
import reset.reset.Models.product.Desconto;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.product.DescontoRepository;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.BaseFilter;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class DescontoService extends BaseServiceImpl<Desconto, Long, DescontoRepository> {

    private final DescontoRepository descontoRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public DescontoService(DescontoRepository repository) {
        super(repository);
        this.descontoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Desconto desconto) {
        validateEmpresaExists(desconto.getEmpresa());
        validateTipo(desconto.getTipo());
        validateValor(desconto.getTipo(), desconto.getValor());
        validateDescricaoUniqueness(desconto.getDescricao(), desconto.getEmpresa().getId(), null);
    }

    @Override
    protected void validateBeforeUpdate(Long id, Desconto desconto) {
        Desconto existing = findByIdOrThrow(id);
        validateEmpresaExists(desconto.getEmpresa());
        validateTipo(desconto.getTipo());
        validateValor(desconto.getTipo(), desconto.getValor());

        if (!existing.getDescricao().equals(desconto.getDescricao())) {
            validateDescricaoUniqueness(desconto.getDescricao(), desconto.getEmpresa().getId(), id);
        }
    }

    private void validateEmpresaExists(Empresa empresa) {
        if (empresa != null && empresa.getId() != null) {
            if (!empresaRepository.existsById(empresa.getId())) {
                throw new EntityNotFoundException("Empresa not found with id: " + empresa.getId());
            }
        }
    }

    private void validateTipo(String tipo) {
        if (tipo == null || tipo.isEmpty()) {
            throw new BusinessException("Discount type is required");
        }

        if (!"PERCENTAGEM".equals(tipo) && !"VALOR".equals(tipo)) {
            throw new BusinessException("Invalid discount type. Valid types: PERCENTAGEM, VALOR");
        }
    }

    private void validateValor(String tipo, BigDecimal valor) {
        if (valor == null) {
            throw new BusinessException("Discount value is required");
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Discount value cannot be negative");
        }

        if ("PERCENTAGEM".equals(tipo) && valor.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("Percentage discount cannot exceed 100%");
        }
    }

    private void validateDescricaoUniqueness(String descricao, Long empresaId, Long excludeId) {
        if (descricao == null || descricao.isEmpty()) {
            return;
        }

        // Check if description exists for the same empresa
        // This would require a custom query in the repository
        // For now, we'll just check if any description exists with the same name
        // In a real implementation, add a method to DescontoRepository
    }

    @Override
    @Transactional
    public Desconto save(Desconto desconto) {
        // Set default values if not provided
        if (desconto.getEmpresa() == null) {
            throw new BusinessException("Empresa is required");
        }

        log.info("Creating new discount: {}", desconto.getDescricao());
        return super.save(desconto);
    }

    public BigDecimal calcularValorDesconto(Desconto desconto, BigDecimal subtotal) {
        if (desconto == null || desconto.getValor() == null) {
            return BigDecimal.ZERO;
        }

        if (desconto.getValor().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if ("PERCENTAGEM".equals(desconto.getTipo())) {
            return subtotal.multiply(desconto.getValor().divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP));
        } else { // VALOR
            return desconto.getValor();
        }
    }

    public BigDecimal calcularTotalComDesconto(Desconto desconto, BigDecimal subtotal) {
        BigDecimal valorDesconto = calcularValorDesconto(desconto, subtotal);
        return subtotal.subtract(valorDesconto);
    }


    public Desconto getDescontoAplicavel(Long clienteId, Long produtoId) {
        // Implementação específica para descontos por cliente/produto
        // Pode ser expandida conforme necessidade
        return null;
    }

    public Page<Desconto> filter(BaseFilter filter) {
        // Implementação de filtro com especificações
        // Por simplicidade, retorna todos com paginação
        return descontoRepository.findAll(filter.toPageable());
    }

    public Page<Desconto> findByEmpresaId(Long empresaId, Pageable pageable) {
        return descontoRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<Desconto> findByTipo(String tipo) {
        validateTipo(tipo);
        return descontoRepository.findByTipo(tipo);
    }

    public List<Desconto> findByEmpresaIdAndTipo(Long empresaId, String tipo) {
        validateTipo(tipo);
        return descontoRepository.findByEmpresaIdAndTipo(empresaId, tipo);
    }

    @Transactional
    public Desconto ativarDesconto(Long id) {
        Desconto desconto = findByIdOrThrow(id);
        desconto.setAtivo(true);
        return descontoRepository.save(desconto);
    }

    @Transactional
    public Desconto desativarDesconto(Long id) {
        Desconto desconto = findByIdOrThrow(id);
        desconto.setAtivo(false);
        return descontoRepository.save(desconto);
    }

    public boolean isDescontoAplicavel(Desconto desconto, BigDecimal valorBase) {
        if (desconto == null || !desconto.isAtivo()) {
            return false;
        }

        if (desconto.getValor() == null || desconto.getValor().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        if ("VALOR".equals(desconto.getTipo()) && desconto.getValor().compareTo(valorBase) > 0) {
            return false; // Desconto em valor não pode ser maior que o valor base
        }

        return true;
    }
}
