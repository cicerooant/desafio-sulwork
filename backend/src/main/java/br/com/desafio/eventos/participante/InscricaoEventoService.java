package br.com.desafio.eventos.participante;

import br.com.desafio.eventos.contribuicao.ContribuicaoRepository;
import br.com.desafio.eventos.evento.Evento;
import br.com.desafio.eventos.evento.EventoRepository;
import br.com.desafio.eventos.evento.StatusEvento;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscricaoEventoService {

    private final InscricaoEventoRepository inscricaoEventoRepository;
    private final EventoRepository eventoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final ContribuicaoRepository contribuicaoRepository;

    public InscricaoEventoService(
            InscricaoEventoRepository inscricaoEventoRepository,
            EventoRepository eventoRepository,
            ColaboradorRepository colaboradorRepository,
            ContribuicaoRepository contribuicaoRepository
    ) {
        this.inscricaoEventoRepository = inscricaoEventoRepository;
        this.eventoRepository = eventoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.contribuicaoRepository = contribuicaoRepository;
    }

    @Transactional
    public InscricaoEventoResponse inscrever(Long eventoId, InscricaoEventoRequest request) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado"));
        validarOperacao(evento);
        Colaborador colaborador = colaboradorRepository.findById(request.colaboradorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Colaborador não encontrado"));

        if (inscricaoEventoRepository.existsByEventoIdAndColaboradorId(eventoId, colaborador.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Colaborador já inscrito neste evento");
        }

        InscricaoEvento inscricao = new InscricaoEvento();
        inscricao.setEvento(evento);
        inscricao.setColaborador(colaborador);
        return InscricaoEventoResponse.de(inscricaoEventoRepository.save(inscricao));
    }

    @Transactional(readOnly = true)
    public List<InscricaoEventoResponse> listarPorEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado");
        }

        return inscricaoEventoRepository.findAllByEventoId(eventoId)
                .stream()
                .map(InscricaoEventoResponse::de)
                .toList();
    }

    @Transactional
    public void cancelarInscricao(Long eventoId, Long inscricaoId) {
        InscricaoEvento inscricao = inscricaoEventoRepository.findByIdAndEventoId(inscricaoId, eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscrição não encontrada"));
        validarOperacao(inscricao.getEvento());
        if (contribuicaoRepository.existsByInscricaoEventoId(inscricaoId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inscrição com contribuições não pode ser cancelada"
            );
        }
        inscricaoEventoRepository.delete(inscricao);
    }

    private void validarOperacao(Evento evento) {
        if (evento.getStatus() != StatusEvento.ABERTO
                || !LocalDateTime.now().isBefore(evento.getPrazoAlteracao())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inscrições só podem ser alteradas em ABERTO antes do prazo"
            );
        }
    }
}
