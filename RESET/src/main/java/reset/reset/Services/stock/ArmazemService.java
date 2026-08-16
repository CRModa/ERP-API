package reset.reset.Services.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.stock.Armazem;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.stock.ArmazemRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.util.List;

@Service
@Slf4j
public class ArmazemService extends BaseServiceImpl<Armazem, Long, ArmazemRepository> {

    private final ArmazemRepository armazemRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public ArmazemService(ArmazemRepository repository) {
        super(repository);
        this.armazemRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Armazem armazem) {
        validateEmpresaExists(armazem.getEmpresa().getId());
    }

    @Override
    protected void validateBeforeUpdate(Long id, Armazem armazem) {
        validateEmpresaExists(armazem.getEmpresa().getId());
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    public Page<Armazem> findByEmpresaId(Long empresaId, Pageable pageable) {
        return armazemRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<Armazem> findAllByEmpresaIdOrderByNome(Long empresaId) {
        return armazemRepository.findAllByEmpresaIdOrderByNome(empresaId);
    }
}
