package br.com.desafio.eventos.contribuicao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ContribuicaoRequest(
        @NotNull Long colaboradorId,
        @NotNull Long itemId,
        @NotNull @Positive Integer quantidade
) {
}
