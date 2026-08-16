package reset.reset.Repositories.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reset.reset.Models.restaurant.ItemCardapio;
import reset.reset.Repositories.BaseRepository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ItemCardapioRepository extends BaseRepository<ItemCardapio, Long> {

    @Query("SELECT i FROM ItemCardapio i WHERE i.empresa.id = :empresaId AND i.ativo = true AND i.disponivel = true")
    Page<ItemCardapio> findAvailableByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT i FROM ItemCardapio i WHERE i.empresa.id = :empresaId AND i.categoria.id = :categoriaId AND i.ativo = true")
    Page<ItemCardapio> findByCategoriaId(@Param("categoriaId") Long categoriaId, Pageable pageable);

    @Query("SELECT i FROM ItemCardapio i WHERE i.empresa.id = :empresaId AND i.destaque = true AND i.ativo = true")
    List<ItemCardapio> findDestaquesByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT i FROM ItemCardapio i WHERE i.empresa.id = :empresaId AND i.nome LIKE %:nome% AND i.ativo = true")
    Page<ItemCardapio> searchByNome(@Param("empresaId") Long empresaId,
                                    @Param("nome") String nome,
                                    Pageable pageable);

    @Query("SELECT i FROM ItemCardapio i WHERE i.empresa.id = :empresaId AND i.preco BETWEEN :precoMin AND :precoMax AND i.ativo = true")
    List<ItemCardapio> findByPrecoRange(@Param("empresaId") Long empresaId,
                                        @Param("precoMin") BigDecimal precoMin,
                                        @Param("precoMax") BigDecimal precoMax);
}
