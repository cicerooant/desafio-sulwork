package br.com.desafio.eventos.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByCategoriaId(Long categoriaId);

    Optional<Item> findByIdAndCategoriaId(Long id, Long categoriaId);

    @Query("""
            SELECT i
            FROM Item i
            JOIN FETCH i.categoria categoria
            JOIN FETCH categoria.evento
            WHERE i.id = :itemId
              AND categoria.evento.id = :eventoId
            """)
    Optional<Item> findByIdAndEventoId(
            @Param("itemId") Long itemId,
            @Param("eventoId") Long eventoId
    );

    boolean existsByCategoriaId(Long categoriaId);
}
