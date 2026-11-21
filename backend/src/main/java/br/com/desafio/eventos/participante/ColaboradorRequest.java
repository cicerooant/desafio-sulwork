package br.com.desafio.eventos.participante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ColaboradorRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @CpfValido String cpf,
        @NotNull Set<RestricaoAlimentar> restricoes
) {
}
