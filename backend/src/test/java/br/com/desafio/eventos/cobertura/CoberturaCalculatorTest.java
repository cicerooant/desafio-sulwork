package br.com.desafio.eventos.cobertura;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoberturaCalculatorTest {

    private final CoberturaCalculator calculator = new CoberturaCalculator();

    @Test
    void deveCalcularCoberturaGeralComoMediaPonderadaPorObrigatoriedade() {
        ResultadoCobertura resultado = calculator.calcular(
                List.of(
                        item(1L, 10L, "Lanches", "Pão", 10, 5, true),
                        item(2L, 20L, "Bebidas", "Suco", 5, 5, false)
                ),
                List.of(
                        new CategoriaEntradaCobertura(10L, "Lanches", 8),
                        new CategoriaEntradaCobertura(20L, "Bebidas", 5)
                )
        );

        assertEquals(2.0 / 3.0, resultado.coberturaGeral(), 0.0001);
        assertEquals(1.0, resultado.categorias().getFirst().cobertura(), 0.0001);
        assertEquals(0.625, resultado.categorias().get(1).cobertura(), 0.0001);
    }

    @Test
    void deveUsarSomaDasNecessidadesQuandoCategoriaNaoTemMeta() {
        ResultadoCobertura resultado = calculator.calcular(
                List.of(
                        item(1L, 10L, "Lanches", "Pão", 4, 2, true),
                        item(2L, 10L, "Lanches", "Bolo", 6, 3, false)
                ),
                List.of(new CategoriaEntradaCobertura(10L, "Lanches", null))
        );

        CategoriaCoberturaCalculada categoria = resultado.categorias().getFirst();
        assertEquals(10, categoria.metaQuantidade());
        assertEquals(5, categoria.quantidadeContribuida());
        assertEquals(5, categoria.quantidadeFaltante());
        assertEquals(0.5, categoria.cobertura(), 0.0001);
    }

    @Test
    void deveLimitarCoberturaEOrdenarListaDoQueFalta() {
        ResultadoCobertura resultado = calculator.calcular(
                List.of(
                        item(1L, 10L, "Lanches", "Opcional", 8, 2, false),
                        item(2L, 10L, "Lanches", "Obrigatório menor", 5, 1, true),
                        item(3L, 10L, "Lanches", "Obrigatório maior", 10, 2, true),
                        item(4L, 10L, "Lanches", "Completo", 2, 4, true)
                ),
                List.of(new CategoriaEntradaCobertura(10L, "Lanches", 5))
        );

        ItemCoberturaCalculada completo = resultado.itens().stream()
                .filter(item -> item.nomeItem().equals("Completo"))
                .findFirst()
                .orElseThrow();
        assertEquals(1.0, completo.cobertura());
        assertEquals(0L, completo.quantidadeFaltante());
        assertEquals(
                List.of("Obrigatório maior", "Obrigatório menor", "Opcional"),
                resultado.oQueFalta().stream().map(ItemCoberturaCalculada::nomeItem).toList()
        );
        assertEquals(
                List.of("Obrigatório maior", "Obrigatório menor"),
                resultado.itensObrigatoriosPendentes().stream().map(ItemCoberturaCalculada::nomeItem).toList()
        );
        assertEquals(0, resultado.categorias().getFirst().quantidadeFaltante());
        assertEquals(1.0, resultado.categorias().getFirst().cobertura());
    }

    @Test
    void deveRetornarCoberturaZeroQuandoNaoHaItens() {
        ResultadoCobertura resultado = calculator.calcular(
                List.of(),
                List.of(new CategoriaEntradaCobertura(10L, "Vazias", null))
        );

        assertEquals(0, resultado.coberturaGeral());
        assertEquals(0, resultado.categorias().getFirst().cobertura());
    }

    private ItemEntradaCobertura item(
            Long itemId,
            Long categoriaId,
            String nomeCategoria,
            String nomeItem,
            int necessario,
            long contribuido,
            boolean obrigatorio
    ) {
        return new ItemEntradaCobertura(
                itemId,
                categoriaId,
                nomeCategoria,
                nomeItem,
                necessario,
                contribuido,
                obrigatorio
        );
    }
}
