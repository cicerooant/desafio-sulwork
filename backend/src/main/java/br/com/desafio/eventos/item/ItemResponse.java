package br.com.desafio.eventos.item;

import java.util.Set;

public record ItemResponse(
        Long id,
        Long categoriaId,
        String nome,
        Integer quantidadeNecessaria,
        Boolean obrigatorio,
        Set<Alergeno> alergenos
) {
    public static ItemResponse de(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getCategoria().getId(),
                item.getNome(),
                item.getQuantidadeNecessaria(),
                item.getObrigatorio(),
                Set.copyOf(item.getAlergenos())
        );
    }
}
