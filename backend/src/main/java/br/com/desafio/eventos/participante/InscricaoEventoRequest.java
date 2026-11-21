package br.com.desafio.eventos.participante;

import jakarta.validation.constraints.NotNull;

public record InscricaoEventoRequest(@NotNull Long colaboradorId) {
}
