package br.com.desafio.eventos.participante;

import java.util.Set;

public record InscricaoEventoResponse(
        Long id,
        Long eventoId,
        Long colaboradorId,
        String nomeColaborador,
        Set<RestricaoAlimentar> restricoes
) {
    public static InscricaoEventoResponse de(InscricaoEvento inscricao) {
        Colaborador colaborador = inscricao.getColaborador();
        return new InscricaoEventoResponse(
                inscricao.getId(),
                inscricao.getEvento().getId(),
                colaborador.getId(),
                colaborador.getNome(),
                Set.copyOf(colaborador.getRestricoes())
        );
    }
}
