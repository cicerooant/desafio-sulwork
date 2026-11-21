package br.com.desafio.eventos.evento;

import jakarta.validation.constraints.NotNull;

public record TransicaoStatusRequest(@NotNull StatusEvento status) {
}
