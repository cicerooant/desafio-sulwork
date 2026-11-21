package br.com.desafio.eventos.evento;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    private static final LocalDateTime DATA_EVENTO = LocalDateTime.of(2027, 5, 10, 12, 0);
    private static final LocalDateTime PRAZO = LocalDateTime.of(2027, 5, 9, 12, 0);

    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private EventoService eventoService;

    @Test
    void deveCriarEventoEmRascunho() {
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventoResponse response = eventoService.criar(request("Almoço", DATA_EVENTO, PRAZO));

        ArgumentCaptor<Evento> captor = ArgumentCaptor.forClass(Evento.class);
        verify(eventoRepository).save(captor.capture());
        assertEquals("Almoço", response.nome());
        assertEquals(StatusEvento.RASCUNHO, response.status());
        assertEquals(StatusEvento.RASCUNHO, captor.getValue().getStatus());
    }

    @Test
    void deveListarEventos() {
        Evento evento = eventoComStatus(StatusEvento.ABERTO);
        when(eventoRepository.findAll()).thenReturn(List.of(evento));

        List<EventoResponse> response = eventoService.listar();

        assertEquals(1, response.size());
        assertEquals(StatusEvento.ABERTO, response.getFirst().status());
    }

    @Test
    void deveRetornarErroAoBuscarEventoInexistente() {
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deveAtualizarEventoEmRascunho() {
        Evento evento = eventoComStatus(StatusEvento.RASCUNHO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        EventoResponse response = eventoService.atualizar(1L, request("Almoço atualizado", DATA_EVENTO, PRAZO));

        assertEquals("Almoço atualizado", response.nome());
        assertEquals(StatusEvento.RASCUNHO, response.status());
    }

    @Test
    void deveAtualizarEventoAbertoQuandoPrazoForAnteriorAoEvento() {
        Evento evento = eventoComStatus(StatusEvento.ABERTO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        EventoResponse response = eventoService.atualizar(
                1L,
                request("Almoço atualizado", DATA_EVENTO, PRAZO)
        );

        assertEquals("Almoço atualizado", response.nome());
        assertEquals(StatusEvento.ABERTO, response.status());
    }

    @Test
    void deveRejeitarEdicaoDeEventoAbertoComPrazoIgualOuPosteriorAoEvento() {
        Evento evento = eventoComStatus(StatusEvento.ABERTO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.atualizar(1L, request("Almoço", DATA_EVENTO, DATA_EVENTO))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Evento", evento.getNome());
    }

    @Test
    void deveImpedirEdicaoDeEventoFinalizadoOuCancelado() {
        for (StatusEvento status : List.of(
                StatusEvento.ENCERRADO,
                StatusEvento.REALIZADO,
                StatusEvento.CANCELADO
        )) {
            Evento evento = eventoComStatus(status);
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> eventoService.atualizar(1L, request("Novo nome", DATA_EVENTO, PRAZO))
            );

            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("Evento", evento.getNome());
        }
    }

    @Test
    void deveRealizarTransicaoValida() {
        Evento evento = eventoComStatus(StatusEvento.RASCUNHO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        EventoResponse response = eventoService.transicionarStatus(1L, StatusEvento.ABERTO);

        assertEquals(StatusEvento.ABERTO, response.status());
        assertEquals(StatusEvento.ABERTO, evento.getStatus());
    }

    @Test
    void deveRejeitarTransicaoInvalida() {
        Evento evento = eventoComStatus(StatusEvento.RASCUNHO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.transicionarStatus(1L, StatusEvento.REALIZADO)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(StatusEvento.RASCUNHO, evento.getStatus());
    }

    @Test
    void deveExcluirSomenteEventoEmRascunho() {
        Evento evento = eventoComStatus(StatusEvento.RASCUNHO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        eventoService.excluir(1L);

        verify(eventoRepository).delete(evento);
    }

    @Test
    void deveImpedirExclusaoDeEventoAberto() {
        Evento evento = eventoComStatus(StatusEvento.ABERTO);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.excluir(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(eventoRepository, never()).delete(any(Evento.class));
    }

    private Evento eventoComStatus(StatusEvento status) {
        Evento evento = new Evento();
        evento.setNome("Evento");
        evento.setDescricao("Descrição");
        evento.setTipo("ALMOCO");
        evento.setDataHora(DATA_EVENTO);
        evento.setPrazoAlteracao(PRAZO);
        evento.setStatus(status);
        return evento;
    }

    private EventoRequest request(String nome, LocalDateTime dataHora, LocalDateTime prazoAlteracao) {
        return new EventoRequest(nome, "Descrição", "ALMOCO", dataHora, prazoAlteracao);
    }
}
