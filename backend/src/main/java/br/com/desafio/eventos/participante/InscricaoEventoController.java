package br.com.desafio.eventos.participante;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/eventos/{eventoId}/inscricoes")
public class InscricaoEventoController {

    private final InscricaoEventoService inscricaoEventoService;

    public InscricaoEventoController(InscricaoEventoService inscricaoEventoService) {
        this.inscricaoEventoService = inscricaoEventoService;
    }

    @PostMapping
    public ResponseEntity<InscricaoEventoResponse> inscrever(
            @PathVariable Long eventoId,
            @Valid @RequestBody InscricaoEventoRequest request
    ) {
        InscricaoEventoResponse inscricao = inscricaoEventoService.inscrever(eventoId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(inscricao.id())
                .toUri();
        return ResponseEntity.created(location).body(inscricao);
    }

    @GetMapping
    public List<InscricaoEventoResponse> listar(@PathVariable Long eventoId) {
        return inscricaoEventoService.listarPorEvento(eventoId);
    }

    @DeleteMapping("/{inscricaoId}")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long eventoId,
            @PathVariable Long inscricaoId
    ) {
        inscricaoEventoService.cancelarInscricao(eventoId, inscricaoId);
        return ResponseEntity.noContent().build();
    }
}
