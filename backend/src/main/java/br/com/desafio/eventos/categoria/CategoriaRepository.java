package br.com.desafio.eventos.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByEventoId(Long eventoId);

    Optional<Categoria> findByIdAndEventoId(Long id, Long eventoId);
}
