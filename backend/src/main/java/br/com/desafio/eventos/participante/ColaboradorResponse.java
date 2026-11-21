package br.com.desafio.eventos.participante;

import java.util.Set;

public record ColaboradorResponse(Long id, String nome, String cpf, Set<RestricaoAlimentar> restricoes) {
    public static ColaboradorResponse de(Colaborador colaborador) {
        return new ColaboradorResponse(
                colaborador.getId(),
                colaborador.getNome(),
                colaborador.getCpf(),
                Set.copyOf(colaborador.getRestricoes())
        );
    }
}
