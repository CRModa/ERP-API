package reset.reset.Services.base;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Repositories.BaseRepository;
import java.io.Serializable;

import java.util.List;
import java.util.Optional;

public abstract class BaseServiceImpl<T, ID extends Serializable, R extends BaseRepository<T, ID>>
        implements BaseService<T, ID> {

    protected final R repository;

    public BaseServiceImpl(R repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public T save(T entity) {
        validateBeforeSave(entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public T update(ID id, T entity) {
        validateBeforeUpdate(id, entity);
        return repository.save(entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public T findByIdOrThrow(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        T entity = findByIdOrThrow(id);
        validateBeforeDelete(entity);
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        validateBeforeDelete(entity);
        repository.delete(entity);
    }

    @Override
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    protected void validateBeforeSave(T entity) {
        // Override for custom validation
    }

    protected void validateBeforeUpdate(ID id, T entity) {
        // Override for custom validation
    }

    protected void validateBeforeDelete(T entity) {
        // Override for custom validation
    }
}
