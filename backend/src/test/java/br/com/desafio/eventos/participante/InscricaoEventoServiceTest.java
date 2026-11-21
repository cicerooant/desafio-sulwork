package br.com.desafio.eventos.participante;

import br.com.desafio.eventos.evento.Evento;
import br.com.desafio.eventos.evento.EventoRepository;
import br.com.desafio.eventos.evento.StatusEvento;
import br.com.desafio.eventos.contribuicao.ContribuicaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InscricaoEventoServiceTest {

    @Mock
    private InscricaoEventoRepository inscricaoEventoRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @Mock
    private ContribuicaoRepository contribuicaoRepository;

    @InjectMocks
    private InscricaoEventoService inscricaoEventoService;

    @Test
    void deveInscreverColaboradorUmaVezNoEvento() {
        Evento evento = eventoAberto();
        when(evento.getId()).thenReturn(1L);
        Colaborador colaborador = colaborador();
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(colaboradorRepository.findById(2L)).thenReturn(Optional.of(colaborador));
        when(inscricaoEventoRepository.existsByEventoIdAndColaboradorId(1L, 2L)).thenReturn(false);
        when(inscricaoEventoRepository.save(any(InscricaoEvento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InscricaoEventoResponse response = inscricaoEventoService.inscrever(1L, new InscricaoEventoRequest(2L));

        assertEquals("Joana", response.nomeColaborador());
        assertEquals(Set.of(RestricaoAlimentar.SEM_GLUTEN), response.restricoes());
        verify(inscricaoEventoRepository).save(any(InscricaoEvento.class));
    }

    @Test
    void deveRejeitarInscricaoDuplicada() {
        Evento evento = eventoAberto();
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        Colaborador colaborador = mock(Colaborador.class);
        when(colaborador.getId()).thenReturn(2L);
        when(colaboradorRepository.findById(2L)).thenReturn(Optional.of(colaborador));
        when(inscricaoEventoRepository.existsByEventoIdAndColaboradorId(1L, 2L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inscricaoEventoService.inscrever(1L, new InscricaoEventoRequest(2L))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(inscricaoEventoRepository, never()).save(any(InscricaoEvento.class));
    }

    @Test
    void deveRetornarErroQuandoEventoOuColaboradorNaoExistir() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inscricaoEventoService.inscrever(1L, new InscricaoEventoRequest(2L))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(colaboradorRepository, never()).findById(2L);
    }

    @Test
    void deveImpedirInscricaoQuandoEventoNaoEstaAbertoOuPrazoPassou() {
        Evento evento = mock(Evento.class);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(evento.getStatus()).thenReturn(StatusEvento.ENCERRADO);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inscricaoEventoService.inscrever(1L, new InscricaoEventoRequest(2L))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(colaboradorRepository, never()).findById(2L);
    }

    @Test
    void deveImpedirInscricaoDepoisDoPrazo() {
        Evento evento = mock(Evento.class);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(evento.getStatus()).thenReturn(StatusEvento.ABERTO);
        when(evento.getPrazoAlteracao()).thenReturn(LocalDateTime.now().minusMinutes(1));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inscricaoEventoService.inscrever(1L, new InscricaoEventoRequest(2L))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(colaboradorRepository, never()).findById(2L);
    }

    @Test
    void deveCancelarInscricaoSemContribuicoesEnquantoEventoEstaAberto() {
        Evento evento = eventoAberto();
        InscricaoEvento inscricao = mock(InscricaoEvento.class);
        when(inscricao.getEvento()).thenReturn(evento);
        when(inscricaoEventoRepository.findByIdAndEventoId(3L, 1L)).thenReturn(Optional.of(inscricao));

        inscricaoEventoService.cancelarInscricao(1L, 3L);

        verify(inscricaoEventoRepository).delete(inscricao);
    }

    @Test
    void deveImpedirCancelamentoDeInscricaoComContribuicoes() {
        Evento evento = eventoAberto();
        InscricaoEvento inscricao = mock(InscricaoEvento.class);
        when(inscricao.getEvento()).thenReturn(evento);
        when(inscricaoEventoRepository.findByIdAndEventoId(3L, 1L)).thenReturn(Optional.of(inscricao));
        when(contribuicaoRepository.existsByInscricaoEventoId(3L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inscricaoEventoService.cancelarInscricao(1L, 3L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(inscricaoEventoRepository, never()).delete(inscricao);
    }

    @Test
    void deveImpedirCancelamentoDeInscricaoDepoisDoPrazo() {
        Evento evento = mock(Evento.class);
        when(evento.getStatus()).thenReturn(StatusEvento.ABERTO);
        when(evento.getPrazoAlteracao()).thenReturn(LocalDateTime.now().minusMinutes(1));
        InscricaoEvento inscricao = mock(InscricaoEvento.class);
        when(inscricao.getEvento()).thenReturn(evento);
        when(inscricaoEventoRepository.findByIdAndEventoId(3L, 1L)).thenReturn(Optional.of(inscricao));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inscricaoEventoService.cancelarInscricao(1L, 3L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(inscricaoEventoRepository, never()).delete(inscricao);
    }

    private Evento eventoAberto() {
        Evento evento = mock(Evento.class);
        when(evento.getStatus()).thenReturn(StatusEvento.ABERTO);
        when(evento.getPrazoAlteracao()).thenReturn(LocalDateTime.of(2100, 1, 1, 0, 0));
        return evento;
    }

    private Colaborador colaborador() {
        Colaborador colaborador = mock(Colaborador.class);
        when(colaborador.getId()).thenReturn(2L);
        when(colaborador.getNome()).thenReturn("Joana");
        when(colaborador.getRestricoes()).thenReturn(Set.of(RestricaoAlimentar.SEM_GLUTEN));
        return colaborador;
    }
}
