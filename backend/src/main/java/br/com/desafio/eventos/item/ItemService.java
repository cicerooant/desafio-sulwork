package br.com.desafio.eventos.item;

import br.com.desafio.eventos.categoria.Categoria;
import br.com.desafio.eventos.categoria.CategoriaRepository;
import br.com.desafio.eventos.contribuicao.ContribuicaoRepository;
import br.com.desafio.eventos.evento.Evento;
import br.com.desafio.eventos.evento.StatusEvento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoriaRepository categoriaRepository;
    private final ContribuicaoRepository contribuicaoRepository;

    public ItemService(
            ItemRepository itemRepository,
            CategoriaRepository categoriaRepository,
            ContribuicaoRepository contribuicaoRepository
    ) {
        this.itemRepository = itemRepository;
        this.categoriaRepository = categoriaRepository;
        this.contribuicaoRepository = contribuicaoRepository;
    }

    @Transactional
    public ItemResponse criar(Long eventoId, Long categoriaId, ItemRequest request) {
        Categoria categoria = buscarCategoria(eventoId, categoriaId);
        validarEdicao(categoria.getEvento());

        Item item = new Item();
        item.setCategoria(categoria);
        aplicarDados(item, request);
        return ItemResponse.de(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> listarPorCategoria(Long eventoId, Long categoriaId) {
        buscarCategoria(eventoId, categoriaId);
        return itemRepository.findAllByCategoriaId(categoriaId)
                .stream()
                .map(ItemResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemResponse buscarPorId(Long eventoId, Long categoriaId, Long itemId) {
        buscarCategoria(eventoId, categoriaId);
        return ItemResponse.de(buscarItem(categoriaId, itemId));
    }

    @Transactional
    public ItemResponse atualizar(Long eventoId, Long categoriaId, Long itemId, ItemRequest request) {
        Categoria categoria = buscarCategoria(eventoId, categoriaId);
        validarEdicao(categoria.getEvento());
        Item item = buscarItem(categoriaId, itemId);

        if (categoria.getEvento().getStatus() == StatusEvento.ABERTO
                && request.quantidadeNecessaria() < contribuicaoRepository.somarQuantidadePorItem(itemId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A quantidade necessária não pode ser menor que o total já contribuído"
            );
        }

        aplicarDados(item, request);
        return ItemResponse.de(item);
    }

    @Transactional
    public void excluir(Long eventoId, Long categoriaId, Long itemId) {
        Categoria categoria = buscarCategoria(eventoId, categoriaId);
        validarEdicao(categoria.getEvento());
        Item item = buscarItem(categoriaId, itemId);
        if (contribuicaoRepository.existsByItemId(itemId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Item com contribuições não pode ser excluído"
            );
        }
        itemRepository.delete(item);
    }

    private Categoria buscarCategoria(Long eventoId, Long categoriaId) {
        return categoriaRepository.findByIdAndEventoId(categoriaId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    private Item buscarItem(Long categoriaId, Long itemId) {
        return itemRepository.findByIdAndCategoriaId(itemId, categoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));
    }

    private void validarEdicao(Evento evento) {
        if (evento.getStatus() == StatusEvento.RASCUNHO) {
            return;
        }
        if (evento.getStatus() == StatusEvento.ABERTO
                && LocalDateTime.now().isBefore(evento.getPrazoAlteracao())) {
            return;
        }
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Itens só podem ser alterados em RASCUNHO ou em ABERTO antes do prazo"
        );
    }

    private void aplicarDados(Item item, ItemRequest request) {
        item.setNome(request.nome());
        item.setQuantidadeNecessaria(request.quantidadeNecessaria());
        item.setObrigatorio(request.obrigatorio());
        item.getAlergenos().clear();
        item.getAlergenos().addAll(request.alergenos());
    }
}
