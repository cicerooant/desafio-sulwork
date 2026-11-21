package br.com.desafio.eventos.categoria;

public record CategoriaResponse(Long id, Long eventoId, String nome, Integer metaQuantidade) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getEvento().getId(),
                categoria.getNome(),
                categoria.getMetaQuantidade()
        );
    }
}
