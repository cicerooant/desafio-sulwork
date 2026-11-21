package br.com.desafio.eventos.cobertura;

import br.com.desafio.eventos.categoria.Categoria;
import br.com.desafio.eventos.categoria.CategoriaRepository;
import br.com.desafio.eventos.contribuicao.ContribuicaoRepository;
import br.com.desafio.eventos.evento.EventoRepository;
import br.com.desafio.eventos.item.Item;
import br.com.desafio.eventos.item.ItemRepository;
import br.com.desafio.eventos.participante.Colaborador;
import br.com.desafio.eventos.participante.InscricaoEvento;
import br.com.desafio.eventos.participante.InscricaoEventoRepository;
import br.com.desafio.eventos.participante.RestricaoAlimentar;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CoberturaService {

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ItemRepository itemRepository;
    private final InscricaoEventoRepository inscricaoEventoRepository;
    private final ContribuicaoRepository contribuicaoRepository;
    private final CoberturaCalculator coberturaCalculator;

    public CoberturaService(
            EventoRepository eventoRepository,
            CategoriaRepository categoriaRepository,
            ItemRepository itemRepository,
            InscricaoEventoRepository inscricaoEventoRepository,
            ContribuicaoRepository contribuicaoRepository,
            CoberturaCalculator coberturaCalculator
    ) {
        this.eventoRepository = eventoRepository;
        this.categoriaRepository = categoriaRepository;
        this.itemRepository = itemRepository;
        this.inscricaoEventoRepository = inscricaoEventoRepository;
        this.contribuicaoRepository = contribuicaoRepository;
        this.coberturaCalculator = coberturaCalculator;
    }

    @Transactional(readOnly = true)
    public CoberturaResponse buscarPorEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado");
        }

        List<Categoria> categorias = categoriaRepository.findAllByEventoId(eventoId);
        Map<Long, Long> quantidadesContribuidas = contribuicaoRepository
                .somarQuantidadePorItemDoEvento(eventoId)
                .stream()
                .collect(Collectors.toMap(
                        ContribuicaoRepository.ItemTotalContribuido::getItemId,
                        ContribuicaoRepository.ItemTotalContribuido::getQuantidadeContribuida
                ));
        List<Item> itens = categorias.stream()
                .flatMap(categoria -> itemRepository.findAllByCategoriaId(categoria.getId()).stream())
                .toList();

        List<ItemEntradaCobertura> itensEntrada = itens.stream()
                .map(item -> new ItemEntradaCobertura(
                        item.getId(),
                        item.getCategoria().getId(),
                        item.getCategoria().getNome(),
                        item.getNome(),
                        item.getQuantidadeNecessaria(),
                        quantidadesContribuidas.getOrDefault(item.getId(), 0L),
                        item.getObrigatorio()
                ))
                .toList();
        List<CategoriaEntradaCobertura> categoriasEntrada = categorias.stream()
                .map(categoria -> new CategoriaEntradaCobertura(
                        categoria.getId(),
                        categoria.getNome(),
                        categoria.getMetaQuantidade()
                ))
                .toList();

        Map<Long, List<ConflitoAlimentar>> conflitosPorItem = calcularConflitos(eventoId, itens);
        ResultadoCobertura resultado = coberturaCalculator.calcular(itensEntrada, categoriasEntrada);

        Map<Long, ItemCoberturaResponse> respostasPorItem = resultado.itens().stream()
                .collect(Collectors.toMap(
                        ItemCoberturaCalculada::itemId,
                        item -> ItemCoberturaResponse.de(
                                item,
                                conflitosPorItem.getOrDefault(item.itemId(), List.of())
                        )
                ));

        return new CoberturaResponse(
                resultado.coberturaGeral(),
                resultado.categorias().stream().map(CategoriaCoberturaResponse::de).toList(),
                resultado.itens().stream().map(item -> respostasPorItem.get(item.itemId())).toList(),
                resultado.itensObrigatoriosPendentes().stream()
                        .map(item -> respostasPorItem.get(item.itemId()))
                        .toList(),
                resultado.oQueFalta().stream()
                        .map(item -> respostasPorItem.get(item.itemId()))
                        .toList()
        );
    }

    private Map<Long, List<ConflitoAlimentar>> calcularConflitos(Long eventoId, List<Item> itens) {
        List<InscricaoEvento> inscricoes = inscricaoEventoRepository.findAllByEventoId(eventoId);
        return itens.stream()
                .collect(Collectors.toMap(
                        Item::getId,
                        item -> inscricoes.stream()
                                .flatMap(inscricao -> conflitosDoItem(item, inscricao).stream())
                                .toList()
                ));
    }

    private List<ConflitoAlimentar> conflitosDoItem(Item item, InscricaoEvento inscricao) {
        Colaborador colaborador = inscricao.getColaborador();
        Set<RestricaoAlimentar> restricoes = colaborador.getRestricoes();
        return ConflitosAlimentares.encontrar(
                colaborador.getId(),
                colaborador.getNome(),
                restricoes,
                item.getAlergenos()
        );
    }
}
