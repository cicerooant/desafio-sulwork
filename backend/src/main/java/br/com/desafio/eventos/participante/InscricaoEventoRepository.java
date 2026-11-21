package br.com.desafio.eventos.participante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscricaoEventoRepository extends JpaRepository<InscricaoEvento, Long> {
    List<InscricaoEvento> findAllByEventoId(Long eventoId);

    Optional<InscricaoEvento> findByIdAndEventoId(Long id, Long eventoId);

    Optional<InscricaoEvento> findByEventoIdAndColaboradorId(Long eventoId, Long colaboradorId);

    boolean existsByEventoIdAndColaboradorId(Long eventoId, Long colaboradorId);

    boolean existsByColaboradorId(Long colaboradorId);
}
