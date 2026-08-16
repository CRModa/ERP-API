package reset.reset.Services.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.Tipos.FaturaProforma;
import reset.reset.Repositories.document.FaturaProformaRepository;
import reset.reset.Services.base.BaseServiceImpl;

@Service
@Slf4j
public class FaturaProformaService extends BaseServiceImpl<FaturaProforma, Long, FaturaProformaRepository> {

    private final FaturaProformaRepository faturaProformaRepository;

    public FaturaProformaService(FaturaProformaRepository repository) {
        super(repository);
        this.faturaProformaRepository = repository;
    }

    @Override
    protected void validateBeforeSave(FaturaProforma faturaProforma) {
        if (faturaProforma.getVencimento() != null &&
                faturaProforma.getVencimento().isBefore(faturaProforma.getData())) {
            throw new BusinessException("Due date cannot be before document date");
        }
        if (faturaProforma.getConvertidaEmFatura() == null) {
            faturaProforma.setConvertidaEmFatura(false);
        }
    }

    @Override
    @Transactional
    public FaturaProforma save(FaturaProforma faturaProforma) {
        if (faturaProforma.getEstado() == null) {
            faturaProforma.setEstado("PENDENTE");
        }
        return super.save(faturaProforma);
    }

    @Transactional
    public FaturaProforma converterParaFatura(Long id, Long faturaId) {
        FaturaProforma faturaProforma = findByIdOrThrow(id);

        if (faturaProforma.getConvertidaEmFatura()) {
            throw new BusinessException("This proforma invoice has already been converted");
        }

        faturaProforma.setConvertidaEmFatura(true);
        faturaProforma.setIdFatura(faturaId);
        faturaProforma.setEstado("CONVERTIDO");
        return faturaProformaRepository.save(faturaProforma);
    }

    @Transactional
    public FaturaProforma aprovarFaturaProforma(Long id) {
        FaturaProforma faturaProforma = findByIdOrThrow(id);

        if (!"PENDENTE".equals(faturaProforma.getEstado())) {
            throw new BusinessException("Only pending proforma invoices can be approved");
        }

        faturaProforma.setEstado("APROVADO");
        return faturaProformaRepository.save(faturaProforma);
    }

    public Page<FaturaProforma> findByEmpresaId(Long empresaId, Pageable pageable) {
        return faturaProformaRepository.findByEmpresaId(empresaId, pageable);
    }
}
