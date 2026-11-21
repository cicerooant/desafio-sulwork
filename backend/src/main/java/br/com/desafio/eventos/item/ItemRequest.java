package br.com.desafio.eventos.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ItemRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotNull @Positive Integer quantidadeNecessaria,
        @NotNull Boolean obrigatorio,
        @NotNull Set<Alergeno> alergenos
) {
}
