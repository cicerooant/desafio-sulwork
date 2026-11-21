package br.com.desafio.eventos.cobertura;

public record CategoriaCoberturaCalculada(
        Long categoriaId,
        String nome,
        long metaQuantidade,
        long quantidadeContribuida,
        long quantidadeFaltante,
        double cobertura
) {
}
