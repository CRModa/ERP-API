package reset.reset.Services.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reset.reset.Exceptions.BusinessException;
import reset.reset.Models.document.Tipos.Cotacao;
import reset.reset.Repositories.document.CotacaoRepository;
import reset.reset.Services.base.BaseServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CotacaoService extends BaseServiceImpl<Cotacao, Long, CotacaoRepository> {

    private final CotacaoRepository cotacaoRepository;

    public CotacaoService(CotacaoRepository repository) {
        super(repository);
        this.cotacaoRepository = repository;
    }

    @Override
    protected void validateBeforeSave(Cotacao cotacao) {
        if (cotacao.getValidoAte() != null && cotacao.getValidoAte().isBefore(cotacao.getData())) {
            throw new BusinessException("Validity date cannot be before document date");
        }
        if (cotacao.getTaxaConversao() != null && cotacao.getTaxaConversao().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Conversion rate must be greater than zero");
        }
    }

    @Override
    @Transactional
    public Cotacao save(Cotacao cotacao) {
        if (cotacao.getEstado() == null) {
            cotacao.setEstado("PENDENTE");
        }
        return super.save(cotacao);
    }

    @Transactional
    public Cotacao aprovarCotacao(Long id) {
        Cotacao cotacao = findByIdOrThrow(id);

        if (!"PENDENTE".equals(cotacao.getEstado())) {
            throw new BusinessException("Only pending quotations can be approved");
        }

        if (cotacao.getValidoAte() != null && cotacao.getValidoAte().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot approve an expired quotation");
        }

        cotacao.setEstado("APROVADO");
        cotacao.setDataAprovacao(LocalDateTime.now());
        return cotacaoRepository.save(cotacao);
    }

    @Transactional
    public Cotacao rejeitarCotacao(Long id, String motivo) {
        Cotacao cotacao = findByIdOrThrow(id);

        if (!"PENDENTE".equals(cotacao.getEstado())) {
            throw new BusinessException("Only pending quotations can be rejected");
        }

        cotacao.setEstado("REJEITADO");
        cotacao.setMotivoRejeicao(motivo);
        return cotacaoRepository.save(cotacao);
    }

    @Transactional
    public Cotacao converterParaFaturaProforma(Long id) {
        Cotacao cotacao = findByIdOrThrow(id);

        if (!"APROVADO".equals(cotacao.getEstado())) {
            throw new BusinessException("Only approved quotations can be converted");
        }

        cotacao.setEstado("CONVERTIDO");
        return cotacaoRepository.save(cotacao);
    }

    public List<Cotacao> findCotacoesExpiradas() {
        return cotacaoRepository.findCotacoesExpiradas(LocalDate.now());
    }

    public List<Cotacao> findCotacoesAprovadas() {
        return cotacaoRepository.findCotacoesAprovadas();
    }

    public List<Cotacao> findCotacoesPendentes() {
        return cotacaoRepository.findCotacoesPendentes();
    }

    public Page<Cotacao> findByEmpresaId(Long empresaId, Pageable pageable) {
        return cotacaoRepository.findByEmpresaId(empresaId, pageable);
    }
}
