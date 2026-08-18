package reset.reset.Services.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.User;
import reset.reset.Models.product.Produto;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.product.CategoriaProdutoRepository;
import reset.reset.Repositories.product.IvaRepository;
import reset.reset.Repositories.product.ProdutoRepository;
import reset.reset.Repositories.stock.StockRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.filter.ProdutoFilter;
import reset.reset.dto.projection.ProdutoResumo;

import java.math.BigDecimal;
import java.util.List;

@Service
//@RequiredArgsConstructor
@Slf4j
public class ProdutoService extends BaseServiceImpl<Produto, Long, ProdutoRepository> {

    private final ProdutoRepository produtoRepository;
    @Autowired
    private CategoriaProdutoRepository categoriaRepository;
    @Autowired
    private IvaRepository ivaRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private UserRepository userRepository;

    public ProdutoService(ProdutoRepository repository) {
        super(repository);
        this.produtoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Produto produto) {
//        validateEmpresaExists(produto.getEmpresa().getId());
        validateCategoriaExists(produto.getCategoria().getId());
        validateIvaExists(produto.getIva().getId());
        validateCodigoUniqueness(produto.getCodigo(), getCurrentEmpresaId(), null);
        validatePrecoVenda(produto.getPrecoVenda());
        validatePrecoCusto(produto.getPrecoCusto());
        produto.setEmpresa(getAuthenticatedUser().getEmpresa());
    }

    @Override
    protected void validateBeforeUpdate(Long id, Produto produto) {
        Produto existing = findByIdOrThrow(id);
        validateEmpresaExists(produto.getEmpresa().getId());
        validateCategoriaExists(produto.getCategoria().getId());
        validateIvaExists(produto.getIva().getId());

        if (!existing.getCodigo().equals(produto.getCodigo())) {
            validateCodigoUniqueness(produto.getCodigo(), produto.getEmpresa().getId(), id);
        }
        validatePrecoVenda(produto.getPrecoVenda());
        validatePrecoCusto(produto.getPrecoCusto());
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateCategoriaExists(Long categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new EntityNotFoundException("Category not found with id: " + categoriaId);
        }
    }

    private void validateIvaExists(Long ivaId) {
        if (!ivaRepository.existsById(ivaId)) {
            throw new EntityNotFoundException("IVA not found with id: " + ivaId);
        }
    }

    private void validateCodigoUniqueness(String codigo, Long empresaId, Long excludeId) {
        produtoRepository.findByCodigo(codigo)
                .ifPresent(p -> {
                    if (excludeId == null || !p.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Product code already exists: " + codigo);
                    }
                });
    }

    private void validatePrecoVenda(BigDecimal preco) {
        if (preco != null && preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Selling price cannot be negative");
        }
    }

    private void validatePrecoCusto(BigDecimal preco) {
        if (preco != null && preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Cost price cannot be negative");
        }
    }

    @Override
    @Transactional
    public Produto save(Produto produto) {
        if (produto.getPrecoVenda() == null) {
            produto.setPrecoVenda(BigDecimal.ZERO);
        }
        if (produto.getPrecoCusto() == null) {
            produto.setPrecoCusto(BigDecimal.ZERO);
        }
        return super.save(produto);
    }

    @Transactional
    public Produto atualizarPrecoVenda(Long id, BigDecimal novoPreco) {
        Produto produto = findByIdOrThrow(id);
        validatePrecoVenda(novoPreco);
        produto.setPrecoVenda(novoPreco);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizarPrecoCusto(Long id, BigDecimal novoPreco) {
        Produto produto = findByIdOrThrow(id);
        validatePrecoCusto(novoPreco);
        produto.setPrecoCusto(novoPreco);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto ativarProduto(Long id) {
        Produto produto = findByIdOrThrow(id);
        produto.setAtivo(true);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto desativarProduto(Long id) {
        Produto produto = findByIdOrThrow(id);
        produto.setAtivo(false);
        return produtoRepository.save(produto);
    }

    public Page<Produto> filter(ProdutoFilter filter) {
        return produtoRepository.filter(filter);
    }

    public Page<Produto> findByEmpresaId(Long empresaId, Pageable pageable) {
        return produtoRepository.findByEmpresaId(empresaId, pageable);
    }

    public Page<Produto> findActiveByEmpresaId(Long empresaId, Pageable pageable) {
        return produtoRepository.findActiveByEmpresaId(empresaId, pageable);
    }

    public Page<ProdutoResumo> findProdutoResumoByEmpresaId(Long empresaId, Pageable pageable) {
        return produtoRepository.findProdutoResumoByEmpresaId(empresaId, pageable);
    }

    public Page<Produto> findByCategoriaId(Long categoriaId, Pageable pageable) {
        return produtoRepository.findByEmpresaIdAndCategoriaId(
                getCurrentEmpresaId(), categoriaId, pageable);
    }

    public List<Produto> findProdutosByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return produtoRepository.findProdutosByPriceRange(minPrice, maxPrice);
    }

    public List<Produto> findActiveByEmpresaIdOrderByNome(Long empresaId) {
        return produtoRepository.findActiveByEmpresaIdOrderByNome(empresaId);
    }

    public long countActiveByEmpresaId(Long empresaId) {
        return produtoRepository.countActiveByEmpresaId(empresaId);
    }

    private Long getCurrentEmpresaId() {
        return getAuthenticatedUser().getEmpresa().getId();
    }

    private User getAuthenticatedUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId()).get();
    }
}
