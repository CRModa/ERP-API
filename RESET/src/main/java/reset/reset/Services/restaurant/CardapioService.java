package reset.reset.Services.restaurant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.restaurant.CategoriaCardapio;
import reset.reset.Models.restaurant.ItemCardapio;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.product.IvaRepository;
import reset.reset.Repositories.restaurant.CategoriaCardapioRepository;
import reset.reset.Repositories.restaurant.ItemCardapioRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardapioService {

    private final CategoriaCardapioRepository categoriaRepository;
    private final ItemCardapioRepository itemRepository;
    private final EmpresaRepository empresaRepository;
    private final IvaRepository ivaRepository;

    // ========== Categoria Cardapio Methods ==========

    @Transactional
    public CategoriaCardapio criarCategoria(CategoriaCardapio categoria) {
        validateEmpresaExists(categoria.getEmpresa().getId());
        validateCategoriaNomeUniqueness(categoria.getEmpresa().getId(), categoria.getNome(), null);

        if (categoria.getOrdem() == null) {
            categoria.setOrdem(0);
        }

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public CategoriaCardapio atualizarCategoria(Long id, CategoriaCardapio categoria) {
        CategoriaCardapio existing = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        validateEmpresaExists(categoria.getEmpresa().getId());

        if (!existing.getNome().equals(categoria.getNome())) {
            validateCategoriaNomeUniqueness(categoria.getEmpresa().getId(), categoria.getNome(), id);
        }

        existing.setNome(categoria.getNome());
        existing.setDescricao(categoria.getDescricao());
        existing.setOrdem(categoria.getOrdem());
        existing.setIcone(categoria.getIcone());

        return categoriaRepository.save(existing);
    }

    private void validateCategoriaNomeUniqueness(Long empresaId, String nome, Long excludeId) {
        if (categoriaRepository.existsByEmpresaIdAndNome(empresaId, nome)) {
            // Check if it's the same category being updated
            if (excludeId != null) {
                CategoriaCardapio existing = categoriaRepository.findById(excludeId).orElse(null);
                if (existing != null && existing.getNome().equals(nome)) {
                    return;
                }
            }
            throw new DuplicateEntityException("Categoria com nome " + nome + " já existe");
        }
    }

    public Page<CategoriaCardapio> findCategoriasByEmpresa(Long empresaId, Pageable pageable) {
        return categoriaRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<CategoriaCardapio> findCategoriasAtivasByEmpresa(Long empresaId) {
        return categoriaRepository.findActiveByEmpresaIdOrderByOrdem(empresaId);
    }

    @Transactional
    public void deletarCategoria(Long id) {
        CategoriaCardapio categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        categoria.setAtivo(false);
        categoriaRepository.save(categoria);
    }

    // ========== Item Cardapio Methods ==========

    @Transactional
    public ItemCardapio criarItem(ItemCardapio item) {
        validateEmpresaExists(item.getEmpresa().getId());
        validateCategoriaExists(item.getCategoria().getId());
        validateIvaExists(item.getIva().getId());
        validatePreco(item.getPreco());
        validateCusto(item.getCusto());

        return itemRepository.save(item);
    }

    @Transactional
    public ItemCardapio atualizarItem(Long id, ItemCardapio item) {
        ItemCardapio existing = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado"));

        validateEmpresaExists(item.getEmpresa().getId());
        validateCategoriaExists(item.getCategoria().getId());
        validateIvaExists(item.getIva().getId());
        validatePreco(item.getPreco());
        validateCusto(item.getCusto());

        existing.setCategoria(item.getCategoria());
        existing.setIva(item.getIva());
        existing.setCodigo(item.getCodigo());
        existing.setNome(item.getNome());
        existing.setDescricao(item.getDescricao());
        existing.setPreco(item.getPreco());
        existing.setCusto(item.getCusto());
        existing.setTempoPreparo(item.getTempoPreparo());
        existing.setIngredientes(item.getIngredientes());
        existing.setInformacaoNutricional(item.getInformacaoNutricional());
        existing.setImagem(item.getImagem());
        existing.setDestaque(item.getDestaque());
        existing.setDisponivel(item.getDisponivel());

        return itemRepository.save(existing);
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa não encontrada");
        }
    }

    private void validateCategoriaExists(Long categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new EntityNotFoundException("Categoria não encontrada");
        }
    }

    private void validateIvaExists(Long ivaId) {
        if (ivaId != null && !ivaRepository.existsById(ivaId)) {
            throw new EntityNotFoundException("IVA não encontrado");
        }
    }

    private void validatePreco(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Preço não pode ser negativo");
        }
    }

    private void validateCusto(BigDecimal custo) {
        if (custo != null && custo.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Custo não pode ser negativo");
        }
    }

    public Page<ItemCardapio> findItensByEmpresa(Long empresaId, Pageable pageable) {
        return itemRepository.findAvailableByEmpresaId(empresaId, pageable);
    }

    public Page<ItemCardapio> findItensByCategoria(Long categoriaId, Pageable pageable) {
        return itemRepository.findByCategoriaId(categoriaId, pageable);
    }

    public List<ItemCardapio> findItensDestaque(Long empresaId) {
        return itemRepository.findDestaquesByEmpresaId(empresaId);
    }

    public Page<ItemCardapio> searchItens(Long empresaId, String nome, Pageable pageable) {
        return itemRepository.searchByNome(empresaId, nome, pageable);
    }

    @Transactional
    public ItemCardapio toggleDisponibilidade(Long id) {
        ItemCardapio item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado"));
        item.setDisponivel(!item.getDisponivel());
        return itemRepository.save(item);
    }
}
