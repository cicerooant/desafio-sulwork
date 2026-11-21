package br.com.desafio.eventos.categoria;

import br.com.desafio.eventos.evento.Evento;
import br.com.desafio.eventos.evento.EventoRepository;
import br.com.desafio.eventos.evento.StatusEvento;
import br.com.desafio.eventos.item.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EventoRepository eventoRepository;
    private final ItemRepository itemRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            EventoRepository eventoRepository,
            ItemRepository itemRepository
    ) {
        this.categoriaRepository = categoriaRepository;
        this.eventoRepository = eventoRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public CategoriaResponse criar(Long eventoId, CategoriaRequest request) {
        Evento evento = buscarEvento(eventoId);
        validarEdicao(evento);

        Categoria categoria = new Categoria();
        categoria.setEvento(evento);
        aplicarDados(categoria, request);
        return CategoriaResponse.de(categoriaRepository.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarPorEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado");
        }

        return categoriaRepository.findAllByEventoId(eventoId)
                .stream()
                .map(CategoriaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long eventoId, Long categoriaId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado");
        }
        return CategoriaResponse.de(buscarCategoria(eventoId, categoriaId));
    }

    @Transactional
    public CategoriaResponse atualizar(Long eventoId, Long categoriaId, CategoriaRequest request) {
        Evento evento = buscarEvento(eventoId);
        validarEdicao(evento);
        Categoria categoria = buscarCategoria(eventoId, categoriaId);
        aplicarDados(categoria, request);
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluir(Long eventoId, Long categoriaId) {
        Evento evento = buscarEvento(eventoId);
        validarEdicao(evento);
        Categoria categoria = buscarCategoria(eventoId, categoriaId);
        if (itemRepository.existsByCategoriaId(categoriaId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Categoria com itens não pode ser excluída"
            );
        }
        categoriaRepository.delete(categoria);
    }

    private Evento buscarEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
    }

    private Categoria buscarCategoria(Long eventoId, Long categoriaId) {
        return categoriaRepository.findByIdAndEventoId(categoriaId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
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
                "Categorias só podem ser alteradas em RASCUNHO ou em ABERTO antes do prazo"
        );
    }

    private void aplicarDados(Categoria categoria, CategoriaRequest request) {
        categoria.setNome(request.nome());
        categoria.setMetaQuantidade(request.metaQuantidade());
    }
}
