package br.com.desafio.eventos.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByCategoriaId(Long categoriaId);

    Optional<Item> findByIdAndCategoriaId(Long id, Long categoriaId);

    boolean existsByCategoriaId(Long categoriaId);
}
