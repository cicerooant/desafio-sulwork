package br.com.desafio.eventos.contribuicao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContribuicaoRepository extends JpaRepository<Contribuicao, Long> {
    interface ItemTotalContribuido {
        Long getItemId();

        Long getQuantidadeContribuida();
    }

    boolean existsByItemId(Long itemId);

    boolean existsByInscricaoEventoId(Long inscricaoEventoId);

    @Query("""
            SELECT c
            FROM Contribuicao c
            JOIN FETCH c.inscricaoEvento inscricao
            JOIN FETCH inscricao.colaborador
            JOIN FETCH c.item item
            JOIN FETCH item.categoria categoria
            WHERE categoria.evento.id = :eventoId
            ORDER BY c.id
            """)
    List<Contribuicao> findAllByEventoId(@Param("eventoId") Long eventoId);

    @Query("""
            SELECT c
            FROM Contribuicao c
            JOIN FETCH c.inscricaoEvento inscricao
            JOIN FETCH inscricao.colaborador
            JOIN FETCH c.item item
            JOIN FETCH item.categoria categoria
            WHERE c.id = :contribuicaoId
              AND categoria.evento.id = :eventoId
            """)
    Optional<Contribuicao> findByIdAndEventoId(
            @Param("contribuicaoId") Long contribuicaoId,
            @Param("eventoId") Long eventoId
    );

    @Query(value = """
            SELECT item.id AS itemId, COALESCE(SUM(contribuicao.quantidade), 0) AS quantidadeContribuida
            FROM item_evento item
            JOIN categoria ON categoria.id = item.categoria_id
            LEFT JOIN contribuicao ON contribuicao.item_id = item.id
            WHERE categoria.evento_id = :eventoId
            GROUP BY item.id
            """, nativeQuery = true)
    List<ItemTotalContribuido> somarQuantidadePorItemDoEvento(@Param("eventoId") Long eventoId);

    @Query(value = """
            SELECT COALESCE(SUM(quantidade), 0)
            FROM contribuicao
            WHERE item_id = :itemId
            """, nativeQuery = true)
    long somarQuantidadePorItem(@Param("itemId") Long itemId);

    @Query(value = """
            SELECT COALESCE(SUM(quantidade), 0)
            FROM contribuicao
            WHERE item_id = :itemId
              AND id <> :contribuicaoId
            """, nativeQuery = true)
    long somarQuantidadePorItemExcluindoContribuicao(
            @Param("itemId") Long itemId,
            @Param("contribuicaoId") Long contribuicaoId
    );
}
