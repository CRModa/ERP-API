package reset.reset.Services.accounting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Exceptions.DuplicateEntityException;
import reset.reset.Exceptions.EntityNotFoundException;
import reset.reset.Models.accounting.Diario;
import reset.reset.Repositories.accounting.DiarioRepository;
import reset.reset.Repositories.core.EmpresaRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.util.List;

@Service
@Slf4j
public class DiarioService extends BaseServiceImpl<Diario, Long, DiarioRepository> {

    private final DiarioRepository diarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;

    public DiarioService(DiarioRepository repository) {
        super(repository);
        this.diarioRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Diario diario) {
        validateEmpresaExists(diario.getEmpresa().getId());
        validateCodigoUniqueness(diario.getCodigo(), diario.getEmpresa().getId(), null);
    }

    @Override
    protected void validateBeforeUpdate(Long id, Diario diario) {
        Diario existing = findByIdOrThrow(id);
        validateEmpresaExists(diario.getEmpresa().getId());

        if (!existing.getCodigo().equals(diario.getCodigo())) {
            validateCodigoUniqueness(diario.getCodigo(), diario.getEmpresa().getId(), id);
        }
    }

    private void validateEmpresaExists(Long empresaId) {
        if (!empresaRepository.existsById(empresaId)) {
            throw new EntityNotFoundException("Empresa not found with id: " + empresaId);
        }
    }

    private void validateCodigoUniqueness(String codigo, Long empresaId, Long excludeId) {
        diarioRepository.findByCodigo(codigo)
                .ifPresent(d -> {
                    if (excludeId == null || !d.getId().equals(excludeId)) {
                        throw new DuplicateEntityException("Journal code already exists: " + codigo);
                    }
                });
    }

    public Page<Diario> findByEmpresaId(Long empresaId, Pageable pageable) {
        return diarioRepository.findByEmpresaId(empresaId, pageable);
    }

    public List<Diario> findAllByEmpresaIdOrderByCodigo(Long empresaId) {
        return diarioRepository.findAllByEmpresaIdOrderByCodigo(empresaId);
    }

    @Transactional
    public Diario criarDiarioPadrao(Long empresaId) {
        // Criar diários padrão
        String[][] diarios = {
                {"DIARIO_GERAL", "Diário Geral"},
                {"DIARIO_VENDAS", "Diário de Vendas"},
                {"DIARIO_COMPRAS", "Diário de Compras"},
                {"DIARIO_CAIXA", "Diário de Caixa"},
                {"DIARIO_BANCO", "Diário de Banco"}
        };

        for (String[] d : diarios) {
            if (!diarioRepository.findByCodigo(d[0]).isPresent()) {
                Diario diario = new Diario();
                diario.setEmpresa(empresaRepository.findById(empresaId)
                        .orElseThrow(() -> new EntityNotFoundException("Empresa not found")));
                diario.setCodigo(d[0]);
                diario.setDescricao(d[1]);
                diarioRepository.save(diario);
            }
        }

        return diarioRepository.findByCodigo("DIARIO_GERAL")
                .orElseThrow(() -> new BusinessException("Failed to create default journal"));
    }
}
