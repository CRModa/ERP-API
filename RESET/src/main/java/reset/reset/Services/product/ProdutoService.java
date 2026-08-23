package reset.reset.Services.product;

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
import reset.reset.Models.product.CategoriaProduto;
import reset.reset.Models.product.Iva;
import reset.reset.Models.product.Produto;
import reset.reset.Models.product.ProdutoCompostoItem;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.product.CategoriaProdutoRepository;
import reset.reset.Repositories.product.IvaRepository;
import reset.reset.Repositories.product.ProdutoCompostoItemRepository;
import reset.reset.Repositories.product.ProdutoRepository;
import reset.reset.Repositories.stock.ArmazemRepository;
import reset.reset.Repositories.stock.StockRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.Services.stock.StockService;
import reset.reset.dto.filter.ProdutoFilter;
import reset.reset.dto.product.ProdutoCompostoDTO;
import reset.reset.dto.product.ProdutoDTO;
import reset.reset.dto.product.ProdutoResumoDTO;
import reset.reset.dto.product.ProdutoRestauranteDTO;
import reset.reset.dto.product.ProdutoCompostoItemDTO;
import reset.reset.dto.request.product.ProdutoCompostoItemRequest;
import reset.reset.dto.request.product.ProdutoCompostoRequest;
import reset.reset.dto.request.product.ProdutoRequest;
import reset.reset.dto.restaurant.CategoriaRestauranteDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
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

    @Autowired
    private ProdutoCompostoItemRepository produtoCompostoItemRepository;

    @Autowired
    private ArmazemRepository armazemRepository;

    @Autowired
    private StockService stockService;

    public ProdutoService(ProdutoRepository repository) {
        super(repository);
        this.produtoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Produto produto) {
        validateCategoriaExists(produto.getCategoria().getId());
        validateIvaExists(produto.getIva().getId());
        validateCodigoUniqueness(produto.getCodigo(), getCurrentEmpresaId(), null);
        validatePrecoVenda(produto.getPrecoVenda());
        validatePrecoCusto(produto.getPrecoCusto());

        User user = getAuthenticatedUser();
        produto.setEmpresa(user.getEmpresa());
        produto.setAtivo(true);

        if (produto.getDisponivel() == null) {
            produto.setDisponivel(true);
        }
        if (produto.getIsComposto() == null) {
            produto.setIsComposto(false);
        }
        if (produto.getDestaque() == null) {
            produto.setDestaque(false);
        }
    }

    @Override
    protected void validateBeforeUpdate(Long id, Produto produto) {
        Produto existing = findByIdOrThrow(id);
        validateCategoriaExists(produto.getCategoria().getId());
        validateIvaExists(produto.getIva().getId());

        if (!existing.getCodigo().equals(produto.getCodigo())) {
            validateCodigoUniqueness(produto.getCodigo(), getCurrentEmpresaId(), id);
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
        if (codigo != null && !codigo.isEmpty()) {
            produtoRepository.findByCodigo(codigo)
                    .ifPresent(p -> {
                        if (excludeId == null || !p.getId().equals(excludeId)) {
                            throw new DuplicateEntityException("Product code already exists: " + codigo);
                        }
                    });
        }
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

    private void validateProdutoFilhoExists(Long produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new EntityNotFoundException("Product not found with id: " + produtoId);
        }
    }

    private void validateNaoPodeSerComposto(Produto produto) {
        if (produto.getIsComposto() != null && produto.getIsComposto()) {
            throw new BusinessException("Cannot add a composed product as an item of another composed product");
        }
    }

    // ==================== CRUD PRODUTO COMPOSTO ====================

    @Transactional
    public ProdutoCompostoDTO criarProdutoComposto(ProdutoCompostoRequest request) {
        Produto produto = new Produto();
        produto.setNome(request.getNome());
        produto.setCodigo(request.getCodigo());
        produto.setDescricao(request.getDescricao());
        produto.setPrecoVenda(request.getPrecoVenda());
        produto.setPrecoCusto(request.getPrecoCusto());
        produto.setTempoPreparo(request.getTempoPreparo());
        produto.setIngredientes(request.getIngredientes());
        produto.setImagem(request.getImagem());
        produto.setDestaque(request.getDestaque());
        produto.setDisponivel(request.getDisponivel());
        produto.setIsComposto(true);
        produto.setAtivo(true);

        CategoriaProduto categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        produto.setCategoria(categoria);

        Iva iva = ivaRepository.findById(request.getIvaId())
                .orElseThrow(() -> new EntityNotFoundException("IVA not found"));
        produto.setIva(iva);

        User user = getAuthenticatedUser();
        produto.setEmpresa(user.getEmpresa());

        Produto saved = produtoRepository.save(produto);

        if (request.getItensComposto() != null && !request.getItensComposto().isEmpty()) {
            Set<ProdutoCompostoItem> itens = new HashSet<>();
            for (ProdutoCompostoItemRequest itemRequest : request.getItensComposto()) {
                Produto produtoFilho = produtoRepository.findById(itemRequest.getProdutoFilhoId())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + itemRequest.getProdutoFilhoId()));

                validateNaoPodeSerComposto(produtoFilho);

                ProdutoCompostoItem item = new ProdutoCompostoItem();
                item.setProdutoPai(saved);
                item.setProdutoFilho(produtoFilho);
                item.setQuantidade(itemRequest.getQuantidade());
                item.setPrecoAdicional(itemRequest.getPrecoAdicional() != null ?
                        itemRequest.getPrecoAdicional() : BigDecimal.ZERO);
                item.setObrigatorio(itemRequest.getObrigatorio() != null ?
                        itemRequest.getObrigatorio() : true);

                itens.add(item);
            }
            saved.setItensComposto(itens);
            saved = produtoRepository.save(saved);
        }

        log.info("Produto composto criado: {}", saved.getNome());
        return toCompostoDTO(saved);
    }

    @Transactional
    public ProdutoCompostoDTO atualizarProdutoComposto(Long id, ProdutoCompostoRequest request) {
        Produto produto = findByIdOrThrow(id);

        if (!produto.getIsComposto()) {
            throw new BusinessException("Product is not a composed product");
        }

        produto.setNome(request.getNome());
        produto.setCodigo(request.getCodigo());
        produto.setDescricao(request.getDescricao());
        produto.setPrecoVenda(request.getPrecoVenda());
        produto.setPrecoCusto(request.getPrecoCusto());
        produto.setTempoPreparo(request.getTempoPreparo());
        produto.setIngredientes(request.getIngredientes());
        produto.setImagem(request.getImagem());
        produto.setDestaque(request.getDestaque());
        produto.setDisponivel(request.getDisponivel());

        CategoriaProduto categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        produto.setCategoria(categoria);

        Iva iva = ivaRepository.findById(request.getIvaId())
                .orElseThrow(() -> new EntityNotFoundException("IVA not found"));
        produto.setIva(iva);

        if (produto.getItensComposto() != null) {
            produtoCompostoItemRepository.deleteAll(produto.getItensComposto());
            produto.getItensComposto().clear();
        }

        if (request.getItensComposto() != null && !request.getItensComposto().isEmpty()) {
            Set<ProdutoCompostoItem> itens = new HashSet<>();
            for (ProdutoCompostoItemRequest itemRequest : request.getItensComposto()) {
                Produto produtoFilho = produtoRepository.findById(itemRequest.getProdutoFilhoId())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + itemRequest.getProdutoFilhoId()));

                validateNaoPodeSerComposto(produtoFilho);

                ProdutoCompostoItem item = new ProdutoCompostoItem();
                item.setProdutoPai(produto);
                item.setProdutoFilho(produtoFilho);
                item.setQuantidade(itemRequest.getQuantidade());
                item.setPrecoAdicional(itemRequest.getPrecoAdicional() != null ?
                        itemRequest.getPrecoAdicional() : BigDecimal.ZERO);
                item.setObrigatorio(itemRequest.getObrigatorio() != null ?
                        itemRequest.getObrigatorio() : true);

                itens.add(item);
            }
            produto.setItensComposto(itens);
        }

        Produto updated = produtoRepository.save(produto);
        log.info("Produto composto atualizado: {}", updated.getNome());
        return toCompostoDTO(updated);
    }

    public ProdutoCompostoDTO findCompostoByIdDTO(Long id) {
        Produto produto = findByIdOrThrow(id);
        if (!produto.getIsComposto()) {
            throw new BusinessException("Product is not a composed product");
        }
        return toCompostoDTO(produto);
    }

    public List<ProdutoCompostoDTO> findCompostosByEmpresaDTO() {
        Long empresaId = getCurrentEmpresaId();
        List<Produto> produtos = produtoRepository.findCompostosByEmpresaId(empresaId);
        return produtos.stream()
                .map(this::toCompostoDTO)
                .collect(Collectors.toList());
    }

    // ==================== CRUD PRODUTO SIMPLES ====================

    @Override
    @Transactional
    public Produto save(Produto produto) {
        if (produto.getPrecoVenda() == null) {
            produto.setPrecoVenda(BigDecimal.ZERO);
        }
        if (produto.getPrecoCusto() == null) {
            produto.setPrecoCusto(BigDecimal.ZERO);
        }
        produto.setIsComposto(false);
        return super.save(produto);
    }

    @Transactional
    public Produto criarProdutoSimples(ProdutoRequest request) {
        Produto produto = request.toEntity();
        User user = getAuthenticatedUser();
        produto.setEmpresa(user.getEmpresa());
        produto.setAtivo(true);
        produto.setIsComposto(false);
        produto.setDisponivel(true);
        return save(produto);
    }

    @Transactional
    public Produto atualizarProdutoSimples(Long id, ProdutoRequest request) {
        Produto produto = findByIdOrThrow(id);
        produto.setNome(request.getNome());
        produto.setCodigo(request.getCodigo());
        produto.setDescricao(request.getDescricao());
        produto.setPrecoVenda(request.getPrecoVenda());
        produto.setPrecoCusto(request.getPrecoCusto());

        CategoriaProduto categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        produto.setCategoria(categoria);

        Iva iva = ivaRepository.findById(request.getIvaId())
                .orElseThrow(() -> new EntityNotFoundException("IVA not found"));
        produto.setIva(iva);

        return produtoRepository.save(produto);
    }

    // ==================== MÉTODOS COM RETORNO DTO ====================

    public Page<ProdutoDTO> filterDTO(ProdutoFilter filter) {
        Page<Produto> produtos = produtoRepository.filter(filter);
        return produtos.map(ProdutoDTO::fromEntity);
    }

    public Page<ProdutoDTO> findByEmpresaIdDTO(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        Page<Produto> produtos = produtoRepository.findByEmpresaId(empresaId, pageable);
        return produtos.map(ProdutoDTO::fromEntity);
    }

    public Page<ProdutoDTO> findActiveByEmpresaIdDTO(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        Page<Produto> produtos = produtoRepository.findActiveByEmpresaId(empresaId, pageable);
        return produtos.map(ProdutoDTO::fromEntity);
    }

    public Page<ProdutoResumoDTO> findProdutoResumoByEmpresaIdDTO(Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        return produtoRepository.findProdutoResumoByEmpresaId(empresaId, pageable)
                .map(this::toProdutoResumoDTO);
    }

    public Page<ProdutoDTO> findByCategoriaIdDTO(Long categoriaId, Pageable pageable) {
        Long empresaId = getCurrentEmpresaId();
        Page<Produto> produtos = produtoRepository.findByEmpresaIdAndCategoriaId(empresaId, categoriaId, pageable);
        return produtos.map(ProdutoDTO::fromEntity);
    }

    public List<ProdutoDTO> findProdutosByPriceRangeDTO(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Produto> produtos = produtoRepository.findProdutosByPriceRange(minPrice, maxPrice);
        return produtos.stream()
                .map(ProdutoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS PARA RESTAURANTE ====================

    // Services/product/ProdutoService.java - Métodos adicionais

    @Transactional
    public Produto toggleDisponibilidade(Long id) {
        Produto produto = findByIdOrThrow(id);
        produto.setDisponivel(!produto.getDisponivel());
        return produtoRepository.save(produto);
    }

    public List<ProdutoRestauranteDTO> findProdutosRestaurante() {
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        List<Produto> produtos = produtoRepository.findByEmpresaIdAndCategoriaVisivelRestaurante(empresaId);
        return produtos.stream()
                .filter(Produto::getAtivo)
                .filter(Produto::getDisponivel)
                .map(this::toRestauranteDTO)
                .collect(Collectors.toList());
    }

    public List<ProdutoRestauranteDTO> findProdutosDestaque() {
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        List<Produto> produtos = produtoRepository.findDestaquesByEmpresaId(empresaId);
        return produtos.stream()
                .filter(Produto::getAtivo)
                .filter(Produto::getDisponivel)
                .map(this::toRestauranteDTO)
                .collect(Collectors.toList());
    }

    public List<CategoriaRestauranteDTO> findCategoriasRestaurante() {
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        List<CategoriaProduto> categorias = categoriaRepository.findByEmpresaIdAndVisivelRestauranteTrue(empresaId);
        return categorias.stream()
                .map(this::toCategoriaRestauranteDTO)
                .collect(Collectors.toList());
    }

    public List<ProdutoRestauranteDTO> findProdutosByCategoriaRestaurante(Long categoriaId) {
        List<Produto> produtos = produtoRepository.findByCategoriaIdAndDisponivelTrue(categoriaId);
        return produtos.stream()
                .filter(Produto::getAtivo)
                .filter(Produto::getDisponivel)
                .map(this::toRestauranteDTO)
                .collect(Collectors.toList());
    }

    public ProdutoRestauranteDTO findProdutoRestauranteById(Long id) {
        Produto produto = findByIdOrThrow(id);
        if (!produto.getAtivo() || !produto.getDisponivel()) {
            throw new BusinessException("Product is not available");
        }
        return toRestauranteDTO(produto);
    }

    // ==================== MÉTODOS DE CONVERSÃO ====================

    private ProdutoResumoDTO toProdutoResumoDTO(reset.reset.dto.projection.ProdutoResumo resumo) {
        BigDecimal stock = stockService.getQuantidadeTotalPorProduto(resumo.getId());
        return ProdutoResumoDTO.builder()
                .id(resumo.getId())
                .codigo(resumo.getCodigo())
                .nome(resumo.getNome())
                .precoVenda(resumo.getPrecoVenda())
                .precoCusto(resumo.getPrecoCusto())
                .quantidadeEstoque(stock)
                .categoriaNome(resumo.getCategoriaNome())
                .ativo(true)
                .disponivel(true)
                .build();
    }

    private CategoriaRestauranteDTO toCategoriaRestauranteDTO(CategoriaProduto categoria) {
        return CategoriaRestauranteDTO.builder()
                .id(categoria.getId())
                .codigo(categoria.getCodigo())
                .descricao(categoria.getDescricao())
                .visivelRestaurante(categoria.getVisivelRestaurante())
                .totalProdutos(categoria.getProdutos() != null ?
                        categoria.getProdutos().stream()
                                .filter(p -> p.getAtivo() && p.getDisponivel())
                                .count() : 0L)
                .produtos(categoria.getProdutos() != null ?
                        categoria.getProdutos().stream()
                                .filter(p -> p.getAtivo() && p.getDisponivel())
                                .map(this::toRestauranteDTO)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    public ProdutoCompostoDTO toCompostoDTO(Produto produto) {
        return ProdutoCompostoDTO.builder()
                .id(produto.getId())
                .codigo(produto.getCodigo())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .precoVenda(produto.getPrecoVenda())
                .precoCusto(produto.getPrecoCusto())
                .ativo(produto.getAtivo())
                .disponivel(produto.getDisponivel())
                .isComposto(produto.getIsComposto())
                .tempoPreparo(produto.getTempoPreparo())
                .ingredientes(produto.getIngredientes())
                .imagem(produto.getImagem())
                .destaque(produto.getDestaque())
                .categoriaId(produto.getCategoria() != null ? produto.getCategoria().getId() : null)
                .categoriaNome(produto.getCategoria() != null ? produto.getCategoria().getDescricao() : null)
                .ivaId(produto.getIva() != null ? produto.getIva().getId() : null)
                .ivaTaxa(produto.getIva() != null ? produto.getIva().getTaxa() : null)
                .itensComposto(produto.getItensComposto() != null ?
                        produto.getItensComposto().stream()
                                .map(this::toCompostoItemDTO)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    private ProdutoCompostoItemDTO toCompostoItemDTO(ProdutoCompostoItem item) {
        return ProdutoCompostoItemDTO.builder()
                .id(item.getId())
                .produtoFilhoId(item.getProdutoFilho() != null ? item.getProdutoFilho().getId() : null)
                .produtoFilhoNome(item.getProdutoFilho() != null ? item.getProdutoFilho().getNome() : null)
                .produtoFilhoCodigo(item.getProdutoFilho() != null ? item.getProdutoFilho().getCodigo() : null)
                .produtoFilhoPrecoVenda(item.getProdutoFilho() != null ? item.getProdutoFilho().getPrecoVenda() : null)
                .quantidade(item.getQuantidade())
                .precoAdicional(item.getPrecoAdicional())
                .obrigatorio(item.getObrigatorio())
                .build();
    }

    private ProdutoRestauranteDTO toRestauranteDTO(Produto produto) {
        return ProdutoRestauranteDTO.builder()
                .id(produto.getId())
                .codigo(produto.getCodigo())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .precoVenda(produto.getPrecoVenda())
                .tempoPreparo(produto.getTempoPreparo())
                .ingredientes(produto.getIngredientes())
                .imagem(produto.getImagem())
                .disponivel(produto.getDisponivel())
                .isComposto(produto.getIsComposto())
                .categoriaId(produto.getCategoria() != null ? produto.getCategoria().getId() : null)
                .categoriaNome(produto.getCategoria() != null ? produto.getCategoria().getDescricao() : null)
                .ivaId(produto.getIva() != null ? produto.getIva().getId() : null)
                .ivaTaxa(produto.getIva() != null ? produto.getIva().getTaxa() : null)
                .itensComposto(produto.getItensComposto() != null && produto.getIsComposto() ?
                        produto.getItensComposto().stream()
                                .map(this::toCompostoItemDTO)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private User getAuthenticatedUser() {
        try {
            UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userRepository.findById(principal.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
        } catch (Exception e) {
            throw new BusinessException("User not authenticated");
        }
    }

    private Long getCurrentEmpresaId() {
        return getAuthenticatedUser().getEmpresa().getId();
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

    public List<Produto> findActiveByEmpresaIdOrderByNome() {
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        return produtoRepository.findActiveByEmpresaIdOrderByNome(empresaId);
    }

    public long countActiveByEmpresaId() {
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        return produtoRepository.countActiveByEmpresaId(empresaId);
    }

    public List<Produto> findProdutosByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return produtoRepository.findProdutosByPriceRange(minPrice, maxPrice);
    }
}