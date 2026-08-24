package reset.reset.Repositories.document;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import reset.reset.Models.document.DocumentoTipo;
import reset.reset.Repositories.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoTipoRepository extends BaseRepository<DocumentoTipo, Long> {

    List<DocumentoTipo> findByClasse(DocumentoTipo.ClasseDocumento classe);
    List<DocumentoTipo> findByMovimentaStockTrue();
    List<DocumentoTipo> findByAfetaContasTrue();
    List<DocumentoTipo> findByNumeracaoAutomaticaTrue();

    @Query("SELECT d FROM DocumentoTipo d WHERE d.classe = :classe ORDER BY d.descricao ASC")
    List<DocumentoTipo> findByClasseOrderByDescricao(DocumentoTipo.ClasseDocumento classe);

    Optional<DocumentoTipo> findByDescricao(String recibo);
}
