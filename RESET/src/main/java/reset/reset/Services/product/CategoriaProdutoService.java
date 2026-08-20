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
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.product.CategoriaProdutoRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;
import reset.reset.dto.product.CategoriaProdutoResponse;
import reset.reset.dto.product.CategoriaProdutoResumoDTO;
import reset.reset.dto.request.product.CategoriaProdutoRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoriaProdutoService extends BaseServiceImpl<CategoriaProduto, Long, CategoriaProdutoRepository> {

    private final CategoriaProdutoRepository categoriaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private UserRepository userRepository;

    public CategoriaProdutoService(CategoriaProdutoRepository repository) {
        super(repository);
        this.categoriaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(CategoriaProduto categoria) {
//        validateEmpresaExists(categoria.getEmpresa().getId());
        categoria.setEmpresa(getAuthenticatedUser().getEmpresa());
        validateCodigoUniqueness(categoria.getCodigo(), categoria.getEmpresa().getId(), null);
        validateDescricaoNotBlank(categoria.getDescricao());
    }

    @Override
    protected void validateBeforeUpdate(Long id, CategoriaProduto categoria) {
        CategoriaProduto existing = findByIdOrThrow(id);
        validateEmpresaExists(categoria.getEmpresa().getId());
        validateDescricaoNotBlank(categoria.getDescricao());

        if (!existing.getCodigo().equals(categoria.getCodigo())) {
            validateCodigoUniqueness(categoria.getCodigo(), categoria.getEmpresa().getId(), id);
        }
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa não encontrada com ID: " + empresaId);
        }
    }

    private void validateCodigoUniqueness(String codigo, Long empresaId, Long excludeId) {
        if (codigo != null && !codigo.isEmpty()) {
            categoriaRepository.findByCodigoAndEmpresaId(codigo, empresaId)
                    .ifPresent(c -> {
                        if (excludeId == null || !c.getId().equals(excludeId)) {
                            throw new DuplicateEntityException("Código já existe: " + codigo);
                        }
                    });
        }
    }

    private void validateDescricaoNotBlank(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new BusinessException("Descrição é obrigatória");
        }
    }

    @Override
    @Transactional
    public CategoriaProduto save(CategoriaProduto categoria) {
        // Valores padrão
        if (categoria.getAtivo() == null) {
            categoria.setAtivo(true);
        }
        if (categoria.getVisivelRestaurante() == null) {
            categoria.setVisivelRestaurante(false);
        }
        if (categoria.getVisivelPos() == null) {
            categoria.setVisivelPos(false);
        }
        if (categoria.getVisivelFarmacia() == null) {
            categoria.setVisivelFarmacia(false);
        }
        if (categoria.getVisivelWeb() == null) {
            categoria.setVisivelWeb(false);
        }

        // Garantir que a empresa está definida
        if (categoria.getEmpresa() == null || categoria.getEmpresa().getId() == null) {
            throw new BusinessException("Empresa é obrigatória");
        }

        return super.save(categoria);
    }

    // ==================== MÉTODOS COM DTO ====================

    @Transactional
    public CategoriaProdutoResponse criarCategoria(CategoriaProdutoRequest request) {
        CategoriaProduto categoria = request.toEntity();
        categoria.setEmpresa(getAuthenticatedUser().getEmpresa());
        CategoriaProduto saved = save(categoria);
        log.info("Categoria criada: {}", saved.getDescricao());
        return CategoriaProdutoResponse.fromEntity(saved);
    }

    @Transactional
    public CategoriaProdutoResponse atualizarCategoria(Long id, CategoriaProdutoRequest request) {
        CategoriaProduto existing = findByIdOrThrow(id);

        // Atualizar campos
        existing.setCodigo(request.getCodigo());
        existing.setDescricao(request.getDescricao());
        existing.setObservacao(request.getObservacao());
        existing.setAtivo(request.getAtivo());
        existing.setVisivelRestaurante(request.getVisivelRestaurante());
        existing.setVisivelPos(request.getVisivelPos());
        existing.setVisivelFarmacia(request.getVisivelFarmacia());
        existing.setVisivelWeb(request.getVisivelWeb());

        CategoriaProduto updated = repository.save(existing);
        log.info("Categoria atualizada: {}", updated.getDescricao());
        return CategoriaProdutoResponse.fromEntity(updated);
    }

    public CategoriaProdutoResponse buscarCategoriaPorId(Long id) {
        CategoriaProduto categoria = findByIdOrThrow(id);
        return CategoriaProdutoResponse.fromEntity(categoria);
    }

    public Page<CategoriaProdutoResumoDTO> listarCategoriasPorEmpresa(Pageable pageable) {
//        validateEmpresaExists(empresaId);
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        Page<CategoriaProduto> categorias = categoriaRepository.findByEmpresaId(empresaId, pageable);
        return categorias.map(this::toResumoDTO);
    }

    public Page<CategoriaProdutoResumoDTO> listarCategoriasAtivasPorEmpresa(Pageable pageable) {
//        validateEmpresaExists(empresaId);
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        Page<CategoriaProduto> categorias = categoriaRepository.findActiveByEmpresaId(empresaId, pageable);
        return categorias.map(this::toResumoDTO);
    }

    public List<CategoriaProdutoResponse> listarTodasCategoriasPorEmpresa() {
//        validateEmpresaExists(empresaId);
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        List<CategoriaProduto> categorias = categoriaRepository.findAllByEmpresaIdOrderByDescricao(empresaId);
        return categorias.stream()
                .map(CategoriaProdutoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CategoriaProdutoResponse> listarCategoriasPorOrdem() {
//        validateEmpresaExists(empresaId);
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        List<CategoriaProduto> categorias = categoriaRepository.findAllByEmpresaIdOrderByOrdem(empresaId);
        return categorias.stream()
                .map(CategoriaProdutoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS POR CANAL ====================

    public List<CategoriaProdutoResponse> listarCategoriasPorCanal(String canal) {
//        validateEmpresaExists(empresaId);
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        List<CategoriaProduto> categorias;

        switch (canal.toUpperCase()) {
            case "RESTAURANTE":
                categorias = categoriaRepository.findByEmpresaIdAndVisivelRestauranteTrue(empresaId);
                break;
            case "POS":
                categorias = categoriaRepository.findByEmpresaIdAndVisivelPosTrue(empresaId);
                break;
            case "FARMACIA":
                categorias = categoriaRepository.findByEmpresaIdAndVisivelFarmaciaTrue(empresaId);
                break;
            case "WEB":
                categorias = categoriaRepository.findByEmpresaIdAndVisivelWebTrue(empresaId);
                break;
            default:
                categorias = categoriaRepository.findVisiveisEmAlgumCanal(empresaId);
                break;
        }

        return categorias.stream()
                .map(CategoriaProdutoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CategoriaProdutoResponse> listarCategoriasVisiveisRestaurante() {
//        validateEmpresaExists(empresaId);
        Long empresaId = getAuthenticatedUser().getEmpresa().getId();
        List<CategoriaProduto> categorias = categoriaRepository.findByEmpresaIdAndVisivelRestauranteTrue(empresaId);
        return categorias.stream()
                .map(CategoriaProdutoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS DE GESTÃO ====================

    @Transactional
    public CategoriaProdutoResponse ativarCategoria(Long id) {
        CategoriaProduto categoria = findByIdOrThrow(id);
        categoria.setAtivo(true);
        CategoriaProduto updated = save(categoria);
        log.info("Categoria ativada: {}", updated.getDescricao());
        return CategoriaProdutoResponse.fromEntity(updated);
    }

    @Transactional
    public CategoriaProdutoResponse desativarCategoria(Long id) {
        CategoriaProduto categoria = findByIdOrThrow(id);
        categoria.setAtivo(false);
        CategoriaProduto updated = save(categoria);
        log.info("Categoria desativada: {}", updated.getDescricao());
        return CategoriaProdutoResponse.fromEntity(updated);
    }

    @Transactional
    public void deletarCategoria(Long id) {
        CategoriaProduto categoria = findByIdOrThrow(id);

        // Verificar se há produtos vinculados
        Long totalProdutos = categoriaRepository.countProdutosAtivosByCategoriaId(id);
        if (totalProdutos > 0) {
            throw new BusinessException("Não é possível excluir a categoria pois existem " + totalProdutos + " produtos vinculados");
        }

        deleteById(id);
        log.info("Categoria excluída: {}", categoria.getDescricao());
    }

    @Transactional
    public CategoriaProdutoResponse atualizarVisibilidadeCanal(Long id, String canal, Boolean visivel) {
        CategoriaProduto categoria = findByIdOrThrow(id);

        switch (canal.toUpperCase()) {
            case "RESTAURANTE":
                categoria.setVisivelRestaurante(visivel);
                break;
            case "POS":
                categoria.setVisivelPos(visivel);
                break;
            case "FARMACIA":
                categoria.setVisivelFarmacia(visivel);
                break;
            case "WEB":
                categoria.setVisivelWeb(visivel);
                break;
            default:
                throw new BusinessException("Canal inválido: " + canal);
        }

        CategoriaProduto updated = save(categoria);
        log.info("Visibilidade atualizada para categoria {} no canal {}: {}",
                updated.getDescricao(), canal, visivel);
        return CategoriaProdutoResponse.fromEntity(updated);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private CategoriaProdutoResumoDTO toResumoDTO(CategoriaProduto categoria) {
        Long totalProdutos = categoriaRepository.countProdutosAtivosByCategoriaId(categoria.getId());
        return CategoriaProdutoResumoDTO.builder()
                .id(categoria.getId())
                .codigo(categoria.getCodigo())
                .descricao(categoria.getDescricao())
                .ativo(categoria.getAtivo())
                .visivelRestaurante(categoria.getVisivelRestaurante())
                .visivelPos(categoria.getVisivelPos())
                .visivelFarmacia(categoria.getVisivelFarmacia())
                .visivelWeb(categoria.getVisivelWeb())
                .totalProdutos(totalProdutos)
                .build();
    }

    // ==================== MÉTODOS LEGADO (Compatibilidade) ====================

    public Page<CategoriaProduto> findByEmpresaId(Long empresaId, Pageable pageable) {
        return categoriaRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<CategoriaProduto> findAllByEmpresaIdOrderByDescricao(Long empresaId) {
        return categoriaRepository.findAllByEmpresaIdOrderByDescricao(empresaId);
    }

    public List<CategoriaProduto> findActiveByEmpresaIdOrderByOrdem(Long empresaId) {
        return categoriaRepository.findByEmpresaIdAndVisivelRestauranteTrue(empresaId);
    }

    private User getAuthenticatedUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId()).get();
    }
}