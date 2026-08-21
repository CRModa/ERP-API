package reset.reset.Services.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.auth.User;
import reset.reset.Models.stock.Armazem;
import reset.reset.Repositories.auth.UserRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Repositories.stock.ArmazemRepository;
import reset.reset.Security.UserPrincipal;
import reset.reset.Services.base.BaseServiceImpl;

import java.util.List;

@Service
@Slf4j
public class ArmazemService extends BaseServiceImpl<Armazem, Long, ArmazemRepository> {

    private final ArmazemRepository armazemRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private UserRepository userRepository;

    public ArmazemService(ArmazemRepository repository) {
        super(repository);
        this.armazemRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Armazem armazem) {
//        validateEmpresaExists(armazem.getEmpresa().getId());
        armazem.setEmpresa(getAuthenticatedUser().getEmpresa());
    }

    @Override
    protected void validateBeforeUpdate(Long id, Armazem armazem) {
//        validateEmpresaExists(armazem.getEmpresa().getId());
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

    private User getAuthenticatedUser() {
        try {
            UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userRepository.findById(principal.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
        } catch (Exception e) {
            throw new BusinessException("User not authenticated");
        }
    }
}
