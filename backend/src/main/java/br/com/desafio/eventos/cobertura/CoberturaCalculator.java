package br.com.desafio.eventos.cobertura;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CoberturaCalculator {

    public ResultadoCobertura calcular(
            List<ItemEntradaCobertura> itensEntrada,
            List<CategoriaEntradaCobertura> categoriasEntrada
    ) {
        List<ItemCoberturaCalculada> itens = itensEntrada.stream()
                .map(this::calcularItem)
                .sorted(Comparator.comparing(ItemCoberturaCalculada::nomeCategoria)
                        .thenComparing(ItemCoberturaCalculada::nomeItem))
                .toList();

        double pesoTotal = itens.stream()
                .mapToInt(item -> item.obrigatorio() ? 2 : 1)
                .sum();
        double somaPonderada = itens.stream()
                .mapToDouble(item -> item.cobertura() * (item.obrigatorio() ? 2 : 1))
                .sum();
        double coberturaGeral = pesoTotal == 0 ? 0 : somaPonderada / pesoTotal;

        Map<Long, Long> contribuidoPorCategoria = itens.stream()
                .collect(Collectors.groupingBy(
                        ItemCoberturaCalculada::categoriaId,
                        Collectors.summingLong(ItemCoberturaCalculada::quantidadeContribuida)
                ));
        Map<Long, Long> necessarioPorCategoria = itens.stream()
                .collect(Collectors.groupingBy(
                        ItemCoberturaCalculada::categoriaId,
                        Collectors.summingLong(ItemCoberturaCalculada::quantidadeNecessaria)
                ));

        List<CategoriaCoberturaCalculada> categorias = categoriasEntrada.stream()
                .map(categoria -> calcularCategoria(
                        categoria,
                        contribuidoPorCategoria.getOrDefault(categoria.categoriaId(), 0L),
                        necessarioPorCategoria.getOrDefault(categoria.categoriaId(), 0L)
                ))
                .sorted(Comparator.comparing(CategoriaCoberturaCalculada::nome))
                .toList();

        Comparator<ItemCoberturaCalculada> ordemFaltantes = Comparator
                .comparing(ItemCoberturaCalculada::obrigatorio)
                .reversed()
                .thenComparing(Comparator.comparingLong(ItemCoberturaCalculada::quantidadeFaltante).reversed())
                .thenComparing(ItemCoberturaCalculada::nomeItem);
        List<ItemCoberturaCalculada> oQueFalta = itens.stream()
                .filter(item -> item.quantidadeFaltante() > 0)
                .sorted(ordemFaltantes)
                .toList();
        List<ItemCoberturaCalculada> obrigatoriosPendentes = oQueFalta.stream()
                .filter(ItemCoberturaCalculada::obrigatorio)
                .toList();

        return new ResultadoCobertura(
                coberturaGeral,
                categorias,
                itens,
                obrigatoriosPendentes,
                oQueFalta
        );
    }

    private ItemCoberturaCalculada calcularItem(ItemEntradaCobertura item) {
        long faltante = Math.max(0, item.quantidadeNecessaria() - item.quantidadeContribuida());
        double cobertura = Math.min(
                (double) item.quantidadeContribuida() / item.quantidadeNecessaria(),
                1.0
        );
        return new ItemCoberturaCalculada(
                item.itemId(),
                item.categoriaId(),
                item.nomeCategoria(),
                item.nomeItem(),
                item.quantidadeNecessaria(),
                item.quantidadeContribuida(),
                faltante,
                cobertura,
                item.obrigatorio()
        );
    }

    private CategoriaCoberturaCalculada calcularCategoria(
            CategoriaEntradaCobertura categoria,
            long quantidadeContribuida,
            long quantidadeNecessaria
    ) {
        long meta = categoria.metaQuantidade() == null
                ? quantidadeNecessaria
                : categoria.metaQuantidade();
        double cobertura = meta == 0
                ? 0
                : Math.min((double) quantidadeContribuida / meta, 1.0);
        return new CategoriaCoberturaCalculada(
                categoria.categoriaId(),
                categoria.nome(),
                meta,
                quantidadeContribuida,
                Math.max(0, meta - quantidadeContribuida),
                cobertura
        );
    }
}
