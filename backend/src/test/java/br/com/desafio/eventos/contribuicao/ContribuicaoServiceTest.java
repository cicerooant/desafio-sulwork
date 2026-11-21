package br.com.desafio.eventos.contribuicao;

import br.com.desafio.eventos.categoria.Categoria;
import br.com.desafio.eventos.evento.Evento;
import br.com.desafio.eventos.evento.EventoRepository;
import br.com.desafio.eventos.evento.StatusEvento;
import br.com.desafio.eventos.item.Item;
import br.com.desafio.eventos.item.ItemRepository;
import br.com.desafio.eventos.participante.Colaborador;
import br.com.desafio.eventos.participante.InscricaoEvento;
import br.com.desafio.eventos.participante.InscricaoEventoRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContribuicaoServiceTest {

    @Mock
    private ContribuicaoRepository contribuicaoRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InscricaoEventoRepository inscricaoEventoRepository;

    @InjectMocks
    private ContribuicaoService contribuicaoService;

    @Test
    void deveCriarContribuicaoParaColaboradorInscrito() {
        Evento evento = eventoAberto();
        InscricaoEvento inscricao = inscricao(10L, "Joana");
        Item item = item(evento, 4);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(inscricaoEventoRepository.findByEventoIdAndColaboradorId(1L, 10L))
                .thenReturn(Optional.of(inscricao));
        when(itemRepository.findByIdAndEventoId(20L, 1L)).thenReturn(Optional.of(item));
        when(contribuicaoRepository.somarQuantidadePorItem(20L)).thenReturn(2L);
        when(contribuicaoRepository.save(any(Contribuicao.class)))
                .thenAnswer(invocation -> {
                    Contribuicao contribuicao = invocation.getArgument(0);
                    return contribuicao;
                });

        ContribuicaoResponse response = contribuicaoService.criar(
                1L,
                new ContribuicaoRequest(10L, 20L, 2)
        );

        assertEquals(1L, response.eventoId());
        assertEquals(10L, response.colaboradorId());
        assertEquals("Joana", response.nomeColaborador());
        assertEquals(20L, response.itemId());
        assertEquals(2, response.quantidade());
    }

    @Test
    void deveRejeitarContribuicaoQueUltrapassaQuantidadeNecessaria() {
        Evento evento = eventoAberto();
        InscricaoEvento inscricao = inscricao(10L, "Joana");
        Item item = item(evento, 5);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(inscricaoEventoRepository.findByEventoIdAndColaboradorId(1L, 10L))
                .thenReturn(Optional.of(inscricao));
        when(itemRepository.findByIdAndEventoId(20L, 1L)).thenReturn(Optional.of(item));
        when(contribuicaoRepository.somarQuantidadePorItem(20L)).thenReturn(4L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contribuicaoService.criar(1L, new ContribuicaoRequest(10L, 20L, 2))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(contribuicaoRepository, never()).save(any(Contribuicao.class));
    }

    @Test
    void deveRejeitarContribuicaoDeColaboradorNaoInscrito() {
        Evento evento = eventoAberto();
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(inscricaoEventoRepository.findByEventoIdAndColaboradorId(1L, 10L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contribuicaoService.criar(1L, new ContribuicaoRequest(10L, 20L, 1))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(itemRepository, never()).findByIdAndEventoId(20L, 1L);
    }

    @Test
    void deveImpedirContribuicaoDepoisDoPrazo() {
        Evento evento = mock(Evento.class);
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(evento.getStatus()).thenReturn(StatusEvento.ABERTO);
        when(evento.getPrazoAlteracao()).thenReturn(LocalDateTime.now().minusMinutes(1));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contribuicaoService.criar(1L, new ContribuicaoRequest(10L, 20L, 1))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(inscricaoEventoRepository, never()).findByEventoIdAndColaboradorId(1L, 10L);
    }

    @Test
    void deveConsiderarSomenteOutrasContribuicoesAoAtualizarMesmoItem() {
        Evento evento = eventoAberto();
        Item item = item(evento, 6);
        InscricaoEvento inscricao = inscricao(10L, "Joana");
        Contribuicao contribuicao = new Contribuicao();
        contribuicao.setItem(item);
        contribuicao.setInscricaoEvento(inscricao);
        contribuicao.setQuantidade(3);

        when(contribuicaoRepository.findByIdAndEventoId(30L, 1L)).thenReturn(Optional.of(contribuicao));
        when(inscricaoEventoRepository.findByEventoIdAndColaboradorId(1L, 10L))
                .thenReturn(Optional.of(inscricao));
        when(itemRepository.findByIdAndEventoId(20L, 1L)).thenReturn(Optional.of(item));
        when(contribuicaoRepository.somarQuantidadePorItemExcluindoContribuicao(20L, 30L)).thenReturn(4L);

        ContribuicaoResponse response = contribuicaoService.atualizar(
                1L,
                30L,
                new ContribuicaoRequest(10L, 20L, 2)
        );

        assertEquals(2, response.quantidade());
        verify(contribuicaoRepository).somarQuantidadePorItemExcluindoContribuicao(20L, 30L);
    }

    @Test
    void deveImpedirAtualizacaoDeContribuicaoDeEventoEncerrado() {
        Evento evento = mock(Evento.class);
        when(evento.getStatus()).thenReturn(StatusEvento.ENCERRADO);
        Item item = item(evento, 6);
        Contribuicao contribuicao = new Contribuicao();
        contribuicao.setItem(item);
        when(contribuicaoRepository.findByIdAndEventoId(30L, 1L)).thenReturn(Optional.of(contribuicao));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> contribuicaoService.atualizar(1L, 30L, new ContribuicaoRequest(10L, 20L, 1))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(inscricaoEventoRepository, never()).findByEventoIdAndColaboradorId(1L, 10L);
    }

    private Evento eventoAberto() {
        Evento evento = mock(Evento.class);
        lenient().when(evento.getId()).thenReturn(1L);
        lenient().when(evento.getStatus()).thenReturn(StatusEvento.ABERTO);
        lenient().when(evento.getPrazoAlteracao()).thenReturn(LocalDateTime.of(2100, 1, 1, 0, 0));
        return evento;
    }

    private Item item(Evento evento, int quantidadeNecessaria) {
        Categoria categoria = mock(Categoria.class);
        lenient().when(categoria.getEvento()).thenReturn(evento);
        Item item = mock(Item.class);
        lenient().when(item.getId()).thenReturn(20L);
        lenient().when(item.getNome()).thenReturn("Pão");
        lenient().when(item.getCategoria()).thenReturn(categoria);
        lenient().when(item.getQuantidadeNecessaria()).thenReturn(quantidadeNecessaria);
        return item;
    }

    private InscricaoEvento inscricao(Long colaboradorId, String nome) {
        Colaborador colaborador = mock(Colaborador.class);
        lenient().when(colaborador.getId()).thenReturn(colaboradorId);
        lenient().when(colaborador.getNome()).thenReturn(nome);
        InscricaoEvento inscricao = mock(InscricaoEvento.class);
        lenient().when(inscricao.getColaborador()).thenReturn(colaborador);
        return inscricao;
    }
}
