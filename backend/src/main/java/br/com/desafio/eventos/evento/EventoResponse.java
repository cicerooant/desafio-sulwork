package br.com.desafio.eventos.evento;

import java.time.LocalDateTime;

public record EventoResponse(
        Long id,
        String nome,
        String descricao,
        String tipo,
        LocalDateTime dataHora,
        LocalDateTime prazoAlteracao,
        StatusEvento status
) {
    public static EventoResponse de(Evento evento) {
        return new EventoResponse(
                evento.getId(),
                evento.getNome(),
                evento.getDescricao(),
                evento.getTipo(),
                evento.getDataHora(),
                evento.getPrazoAlteracao(),
                evento.getStatus()
        );
    }
}
