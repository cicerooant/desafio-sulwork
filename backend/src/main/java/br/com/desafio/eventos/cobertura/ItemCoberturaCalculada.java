package br.com.desafio.eventos.cobertura;

public record ItemCoberturaCalculada(
        Long itemId,
        Long categoriaId,
        String nomeCategoria,
        String nomeItem,
        int quantidadeNecessaria,
        long quantidadeContribuida,
        long quantidadeFaltante,
        double cobertura,
        boolean obrigatorio
) {
}
