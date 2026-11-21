package br.com.desafio.eventos.evento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 2000) String descricao,
        @NotBlank @Size(max = 60) String tipo,
        @NotNull LocalDateTime dataHora,
        @NotNull LocalDateTime prazoAlteracao
) {
}
