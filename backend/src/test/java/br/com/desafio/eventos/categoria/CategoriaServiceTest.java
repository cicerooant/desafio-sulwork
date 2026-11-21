package br.com.desafio.eventos.categoria;

import br.com.desafio.eventos.evento.Evento;
import br.com.desafio.eventos.evento.EventoRepository;
import br.com.desafio.eventos.evento.StatusEvento;
import br.com.desafio.eventos.item.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void deveCriarCategoriaEmRascunho() {
        Evento evento = evento(StatusEvento.RASCUNHO, null);
        when(evento.getId()).thenReturn(1L);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaResponse response = categoriaService.criar(1L, new CategoriaRequest("Bebidas", 10));

        assertEquals("Bebidas", response.nome());
        assertEquals(10, response.metaQuantidade());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void devePermitirCriacaoEmEventoAbertoAntesDoPrazo() {
        Evento evento = evento(StatusEvento.ABERTO, LocalDateTime.now().plusHours(1));
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        categoriaService.criar(1L, new CategoriaRequest("Bebidas", 10));

        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void deveRejeitarAlteracaoDepoisDoPrazoOuEmStatusFinal() {
        for (StatusEvento status : new StatusEvento[]{StatusEvento.ABERTO, StatusEvento.ENCERRADO}) {
            LocalDateTime prazo = status == StatusEvento.ABERTO
                    ? LocalDateTime.now().minusMinutes(1)
                    : null;
            Evento evento = evento(status, prazo);
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> categoriaService.criar(1L, new CategoriaRequest("Bebidas", 10))
            );

            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        }

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void deveImpedirExclusaoDeCategoriaComItens() {
        Evento evento = evento(StatusEvento.RASCUNHO, null);
        Categoria categoria = new Categoria();
        categoria.setEvento(evento);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(categoriaRepository.findByIdAndEventoId(2L, 1L)).thenReturn(Optional.of(categoria));
        when(itemRepository.existsByCategoriaId(2L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.excluir(1L, 2L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }

    private Evento evento(StatusEvento status, LocalDateTime prazo) {
        Evento evento = mock(Evento.class);
        when(evento.getStatus()).thenReturn(status);
        if (prazo != null) {
            when(evento.getPrazoAlteracao()).thenReturn(prazo);
        }
        return evento;
    }
}
