package reset.reset.Repositories.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.Tipos.FaturaProforma;
import reset.reset.Repositories.BaseRepository;

@Repository
public interface FaturaProformaRepository extends BaseRepository<FaturaProforma, Long> {

    Page<FaturaProforma> findByEmpresaId(Long empresaId, Pageable pageable);
}
