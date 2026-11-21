package br.com.desafio.eventos.cobertura;

import br.com.desafio.eventos.participante.RestricaoAlimentar;

import java.util.List;

public record ItemCoberturaResponse(
        Long itemId,
        Long categoriaId,
        String nomeCategoria,
        String nomeItem,
        int quantidadeNecessaria,
        long quantidadeContribuida,
        long quantidadeFaltante,
        double cobertura,
        boolean obrigatorio,
        List<ConflitoAlimentar> conflitos
) {
    public static ItemCoberturaResponse de(
            ItemCoberturaCalculada item,
            List<ConflitoAlimentar> conflitos
    ) {
        return new ItemCoberturaResponse(
                item.itemId(),
                item.categoriaId(),
                item.nomeCategoria(),
                item.nomeItem(),
                item.quantidadeNecessaria(),
                item.quantidadeContribuida(),
                item.quantidadeFaltante(),
                item.cobertura(),
                item.obrigatorio(),
                conflitos
        );
    }
}
