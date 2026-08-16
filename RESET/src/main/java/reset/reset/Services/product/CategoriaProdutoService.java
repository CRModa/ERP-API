package reset.reset.Services.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.product.CategoriaProduto;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.product.CategoriaProdutoRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.util.List;

@Service
//@RequiredArgsConstructor
@Slf4j
public class CategoriaProdutoService extends BaseServiceImpl<CategoriaProduto, Long, CategoriaProdutoRepository> {

    private final CategoriaProdutoRepository categoriaRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public CategoriaProdutoService(CategoriaProdutoRepository repository) {
        super(repository);
        this.categoriaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(CategoriaProduto categoria) {
        validateEmpresaExists(categoria.getEmpresa().getId());
        validateCodigoUniqueness(categoria.getCodigo(), categoria.getEmpresa().getId(), null);
    }

    @Override
    protected void validateBeforeUpdate(Long id, CategoriaProduto categoria) {
        CategoriaProduto existing = findByIdOrThrow(id);
        validateEmpresaExists(categoria.getEmpresa().getId());

        if (!existing.getCodigo().equals(categoria.getCodigo())) {
            validateCodigoUniqueness(categoria.getCodigo(), categoria.getEmpresa().getId(), id);
        }
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateCodigoUniqueness(String codigo, Long empresaId, Long excludeId) {
        if (categoriaRepository.existsByCodigoAndEmpresaId(codigo, empresaId)) {
            CategoriaProduto existing = categoriaRepository.findByCodigo(codigo).get();
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new DuplicateEntityException("Category code already exists: " + codigo);
            }
        }
    }

    public Page<CategoriaProduto> findByEmpresaId(Long empresaId, Pageable pageable) {
        return categoriaRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<CategoriaProduto> findAllByEmpresaIdOrderByDescricao(Long empresaId) {
        return categoriaRepository.findAllByEmpresaIdOrderByDescricao(empresaId);
    }
}
