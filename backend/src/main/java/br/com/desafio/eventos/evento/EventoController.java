package br.com.desafio.eventos.evento;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @PostMapping
    public ResponseEntity<EventoResponse> criar(@Valid @RequestBody EventoRequest request) {
        EventoResponse evento = eventoService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(evento.id())
                .toUri();
        return ResponseEntity.created(location).body(evento);
    }

    @GetMapping
    public List<EventoResponse> listar() {
        return eventoService.listar();
    }

    @GetMapping("/{id}")
    public EventoResponse buscarPorId(@PathVariable Long id) {
        return eventoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public EventoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EventoRequest request) {
        return eventoService.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    public EventoResponse transicionarStatus(
            @PathVariable Long id,
            @Valid @RequestBody TransicaoStatusRequest request
    ) {
        return eventoService.transicionarStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        eventoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
