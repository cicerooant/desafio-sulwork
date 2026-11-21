package br.com.desafio.eventos.item;

import br.com.desafio.eventos.categoria.Categoria;
import br.com.desafio.eventos.categoria.CategoriaRepository;
import br.com.desafio.eventos.contribuicao.ContribuicaoRepository;
import br.com.desafio.eventos.evento.Evento;
import br.com.desafio.eventos.evento.StatusEvento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ContribuicaoRepository contribuicaoRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void deveCriarItemComAlergenos() {
        Categoria categoria = categoria(StatusEvento.RASCUNHO, null);
        when(categoria.getId()).thenReturn(2L);
        when(categoriaRepository.findByIdAndEventoId(2L, 1L)).thenReturn(Optional.of(categoria));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = itemService.criar(
                1L,
                2L,
                new ItemRequest("Pão", 5, true, Set.of(Alergeno.GLUTEN))
        );

        assertEquals("Pão", response.nome());
        assertEquals(Set.of(Alergeno.GLUTEN), response.alergenos());
    }

    @Test
    void deveImpedirCriacaoDepoisDoPrazo() {
        Categoria categoria = categoria(StatusEvento.ABERTO, LocalDateTime.now().minusMinutes(1));
        when(categoriaRepository.findByIdAndEventoId(2L, 1L)).thenReturn(Optional.of(categoria));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> itemService.criar(1L, 2L, new ItemRequest("Pão", 5, true, Set.of()))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void deveRejeitarReducaoAbaixoDasContribuicoesEmEventoAberto() {
        Categoria categoria = categoria(StatusEvento.ABERTO, LocalDateTime.now().plusHours(1));
        Item item = new Item();
        item.setCategoria(categoria);
        item.setNome("Pão");
        item.setQuantidadeNecessaria(5);
        item.setObrigatorio(true);
        when(categoriaRepository.findByIdAndEventoId(2L, 1L)).thenReturn(Optional.of(categoria));
        when(itemRepository.findByIdAndCategoriaId(3L, 2L)).thenReturn(Optional.of(item));
        when(contribuicaoRepository.somarQuantidadePorItem(3L)).thenReturn(5L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> itemService.atualizar(
                        1L,
                        2L,
                        3L,
                        new ItemRequest("Pão", 4, true, Set.of())
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(5, item.getQuantidadeNecessaria());
    }

    @Test
    void devePermitirReducaoAteTotalContribuido() {
        Categoria categoria = categoria(StatusEvento.ABERTO, LocalDateTime.now().plusHours(1));
        when(categoria.getId()).thenReturn(2L);
        Item item = new Item();
        item.setCategoria(categoria);
        item.setNome("Pão");
        item.setQuantidadeNecessaria(8);
        item.setObrigatorio(true);
        when(categoriaRepository.findByIdAndEventoId(2L, 1L)).thenReturn(Optional.of(categoria));
        when(itemRepository.findByIdAndCategoriaId(3L, 2L)).thenReturn(Optional.of(item));
        when(contribuicaoRepository.somarQuantidadePorItem(3L)).thenReturn(5L);

        ItemResponse response = itemService.atualizar(
                1L,
                2L,
                3L,
                new ItemRequest("Pão", 5, true, Set.of())
        );

        assertEquals(5, response.quantidadeNecessaria());
    }

    @Test
    void deveImpedirExclusaoDeItemComContribuicoes() {
        Categoria categoria = categoria(StatusEvento.RASCUNHO, null);
        Item item = new Item();
        item.setCategoria(categoria);
        when(categoriaRepository.findByIdAndEventoId(2L, 1L)).thenReturn(Optional.of(categoria));
        when(itemRepository.findByIdAndCategoriaId(3L, 2L)).thenReturn(Optional.of(item));
        when(contribuicaoRepository.existsByItemId(3L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> itemService.excluir(1L, 2L, 3L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(itemRepository, never()).delete(any(Item.class));
    }

    private Categoria categoria(StatusEvento status, LocalDateTime prazo) {
        Evento evento = mock(Evento.class);
        when(evento.getStatus()).thenReturn(status);
        if (prazo != null) {
            when(evento.getPrazoAlteracao()).thenReturn(prazo);
        }
        Categoria categoria = mock(Categoria.class);
        when(categoria.getEvento()).thenReturn(evento);
        return categoria;
    }
}
