package br.com.desafio.eventos.cobertura;

import br.com.desafio.eventos.item.Alergeno;
import br.com.desafio.eventos.participante.RestricaoAlimentar;

public record ConflitoAlimentar(
        Long colaboradorId,
        String nomeColaborador,
        RestricaoAlimentar restricao,
        Alergeno alergeno,
        NivelConflitoAlimentar nivel
) {
}
