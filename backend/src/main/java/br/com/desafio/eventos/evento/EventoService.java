package br.com.desafio.eventos.evento;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Transactional
    public EventoResponse criar(EventoRequest request) {
        Evento evento = new Evento();
        aplicarDados(evento, request);
        evento.setStatus(StatusEvento.RASCUNHO);
        return EventoResponse.de(eventoRepository.save(evento));
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listar() {
        return eventoRepository.findAll()
                .stream()
                .map(EventoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse buscarPorId(Long id) {
        return EventoResponse.de(buscarEvento(id));
    }

    @Transactional
    public EventoResponse atualizar(Long id, EventoRequest request) {
        Evento evento = buscarEvento(id);
        if (evento.getStatus() != StatusEvento.RASCUNHO && evento.getStatus() != StatusEvento.ABERTO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Somente eventos em RASCUNHO ou ABERTO podem ser editados"
            );
        }

        if (evento.getStatus() == StatusEvento.ABERTO
                && !request.prazoAlteracao().isBefore(request.dataHora())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O prazo para alterações deve ser anterior à data e horário do evento"
            );
        }

        aplicarDados(evento, request);
        return EventoResponse.de(evento);
    }

    @Transactional
    public EventoResponse transicionarStatus(Long id, StatusEvento novoStatus) {
        Evento evento = buscarEvento(id);
        if (!evento.getStatus().podeTransicionarPara(novoStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Transição de " + evento.getStatus() + " para " + novoStatus + " não permitida"
            );
        }

        evento.setStatus(novoStatus);
        return EventoResponse.de(evento);
    }

    @Transactional
    public void excluir(Long id) {
        Evento evento = buscarEvento(id);
        if (evento.getStatus() != StatusEvento.RASCUNHO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Somente eventos em RASCUNHO podem ser excluídos"
            );
        }

        eventoRepository.delete(evento);
    }

    private Evento buscarEvento(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
    }

    private void aplicarDados(Evento evento, EventoRequest request) {
        evento.setNome(request.nome());
        evento.setDescricao(request.descricao());
        evento.setTipo(request.tipo());
        evento.setDataHora(request.dataHora());
        evento.setPrazoAlteracao(request.prazoAlteracao());
    }
}
