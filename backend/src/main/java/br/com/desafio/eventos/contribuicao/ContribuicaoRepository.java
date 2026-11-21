package br.com.desafio.eventos.contribuicao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContribuicaoRepository extends JpaRepository<Contribuicao, Long> {
    boolean existsByItemId(Long itemId);

    boolean existsByInscricaoEventoId(Long inscricaoEventoId);

    @Query(value = """
            SELECT COALESCE(SUM(quantidade), 0)
            FROM contribuicao
            WHERE item_id = :itemId
            """, nativeQuery = true)
    long somarQuantidadePorItem(@Param("itemId") Long itemId);
}
