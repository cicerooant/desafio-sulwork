package br.com.desafio.eventos.contribuicao;

import br.com.desafio.eventos.evento.Evento;
import br.com.desafio.eventos.evento.EventoRepository;
import br.com.desafio.eventos.evento.StatusEvento;
import br.com.desafio.eventos.item.Item;
import br.com.desafio.eventos.item.ItemRepository;
import br.com.desafio.eventos.participante.InscricaoEvento;
import br.com.desafio.eventos.participante.InscricaoEventoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContribuicaoService {

    private final ContribuicaoRepository contribuicaoRepository;
    private final EventoRepository eventoRepository;
    private final ItemRepository itemRepository;
    private final InscricaoEventoRepository inscricaoEventoRepository;

    public ContribuicaoService(
            ContribuicaoRepository contribuicaoRepository,
            EventoRepository eventoRepository,
            ItemRepository itemRepository,
            InscricaoEventoRepository inscricaoEventoRepository
    ) {
        this.contribuicaoRepository = contribuicaoRepository;
        this.eventoRepository = eventoRepository;
        this.itemRepository = itemRepository;
        this.inscricaoEventoRepository = inscricaoEventoRepository;
    }

    @Transactional
    public ContribuicaoResponse criar(Long eventoId, ContribuicaoRequest request) {
        Evento evento = buscarEvento(eventoId);
        validarEventoAbertoAntesDoPrazo(evento);
        InscricaoEvento inscricao = buscarInscricao(eventoId, request.colaboradorId());
        Item item = buscarItem(eventoId, request.itemId());
        validarLimite(item, request.quantidade(), contribuicaoRepository.somarQuantidadePorItem(item.getId()));

        Contribuicao contribuicao = new Contribuicao();
        contribuicao.setInscricaoEvento(inscricao);
        contribuicao.setItem(item);
        contribuicao.setQuantidade(request.quantidade());
        return ContribuicaoResponse.de(contribuicaoRepository.save(contribuicao));
    }

    @Transactional(readOnly = true)
    public List<ContribuicaoResponse> listarPorEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado");
        }

        return contribuicaoRepository.findAllByEventoId(eventoId)
                .stream()
                .map(ContribuicaoResponse::de)
                .toList();
    }

    @Transactional
    public ContribuicaoResponse atualizar(
            Long eventoId,
            Long contribuicaoId,
            ContribuicaoRequest request
    ) {
        Contribuicao contribuicao = buscarContribuicao(eventoId, contribuicaoId);
        validarEventoAbertoAntesDoPrazo(contribuicao.getItem().getCategoria().getEvento());

        InscricaoEvento inscricao = buscarInscricao(eventoId, request.colaboradorId());
        Item item = buscarItem(eventoId, request.itemId());
        long totalExistente = item.getId().equals(contribuicao.getItem().getId())
                ? contribuicaoRepository.somarQuantidadePorItemExcluindoContribuicao(item.getId(), contribuicaoId)
                : contribuicaoRepository.somarQuantidadePorItem(item.getId());
        validarLimite(item, request.quantidade(), totalExistente);

        contribuicao.setInscricaoEvento(inscricao);
        contribuicao.setItem(item);
        contribuicao.setQuantidade(request.quantidade());
        return ContribuicaoResponse.de(contribuicao);
    }

    @Transactional
    public void excluir(Long eventoId, Long contribuicaoId) {
        Contribuicao contribuicao = buscarContribuicao(eventoId, contribuicaoId);
        validarEventoAbertoAntesDoPrazo(contribuicao.getItem().getCategoria().getEvento());
        contribuicaoRepository.delete(contribuicao);
    }

    private Evento buscarEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
    }

    private InscricaoEvento buscarInscricao(Long eventoId, Long colaboradorId) {
        return inscricaoEventoRepository.findByEventoIdAndColaboradorId(eventoId, colaboradorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Somente colaboradores inscritos no evento podem contribuir"
                ));
    }

    private Item buscarItem(Long eventoId, Long itemId) {
        return itemRepository.findByIdAndEventoId(itemId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Item não encontrado neste evento"
                ));
    }

    private Contribuicao buscarContribuicao(Long eventoId, Long contribuicaoId) {
        return contribuicaoRepository.findByIdAndEventoId(contribuicaoId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contribuição não encontrada neste evento"
                ));
    }

    private void validarEventoAbertoAntesDoPrazo(Evento evento) {
        if (evento.getStatus() != StatusEvento.ABERTO
                || !LocalDateTime.now().isBefore(evento.getPrazoAlteracao())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Contribuições só podem ser alteradas em ABERTO antes do prazo"
            );
        }
    }

    private void validarLimite(Item item, int quantidadeSolicitada, long quantidadeExistente) {
        if (quantidadeExistente + quantidadeSolicitada > item.getQuantidadeNecessaria()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A soma das contribuições não pode ultrapassar a quantidade necessária do item"
            );
        }
    }
}
