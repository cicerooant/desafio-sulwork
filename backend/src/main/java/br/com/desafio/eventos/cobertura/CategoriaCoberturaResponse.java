package br.com.desafio.eventos.cobertura;

public record CategoriaCoberturaResponse(
        Long categoriaId,
        String nome,
        long metaQuantidade,
        long quantidadeContribuida,
        long quantidadeFaltante,
        double cobertura
) {
    public static CategoriaCoberturaResponse de(CategoriaCoberturaCalculada categoria) {
        return new CategoriaCoberturaResponse(
                categoria.categoriaId(),
                categoria.nome(),
                categoria.metaQuantidade(),
                categoria.quantidadeContribuida(),
                categoria.quantidadeFaltante(),
                categoria.cobertura()
        );
    }
}
