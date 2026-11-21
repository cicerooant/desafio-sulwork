package br.com.desafio.eventos.cobertura;

public record ItemEntradaCobertura(
        Long itemId,
        Long categoriaId,
        String nomeCategoria,
        String nomeItem,
        int quantidadeNecessaria,
        long quantidadeContribuida,
        boolean obrigatorio
) {
}
