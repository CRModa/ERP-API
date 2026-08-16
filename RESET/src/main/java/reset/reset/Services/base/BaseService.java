package reset.reset.Services.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BaseService<T, ID> {

    T save(T entity);
    T update(ID id, T entity);
    Optional<T> findById(ID id);
    T findByIdOrThrow(ID id);
    Page<T> findAll(Pageable pageable);
    List<T> findAll();
    void deleteById(ID id);
    void delete(T entity);
    boolean existsById(ID id);
    long count();
}

