package br.com.desafio.eventos.evento;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Eventos")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida."),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado."),
        @ApiResponse(responseCode = "409", description = "Conflito com o estado ou integridade dos dados."),
        @ApiResponse(responseCode = "500", description = "Erro inesperado.")
})
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @PostMapping
    @Operation(summary = "Criar evento", description = "Cria um evento em RASCUNHO.")
    @ApiResponse(responseCode = "201", description = "Evento criado.")
    public ResponseEntity<EventoResponse> criar(@Valid @RequestBody EventoRequest request) {
        EventoResponse evento = eventoService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(evento.id())
                .toUri();
        return ResponseEntity.created(location).body(evento);
    }

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Retorna todos os eventos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Eventos encontrados.")
    public List<EventoResponse> listar() {
        return eventoService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento", description = "Retorna os dados de um evento pelo identificador.")
    @ApiResponse(responseCode = "200", description = "Evento encontrado.")
    public EventoResponse buscarPorId(@PathVariable Long id) {
        return eventoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar evento",
            description = "Permite edição apenas em RASCUNHO ou ABERTO; em ABERTO, o prazo deve anteceder o evento."
    )
    @ApiResponse(responseCode = "200", description = "Evento atualizado.")
    public EventoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EventoRequest request) {
        return eventoService.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Alterar status do evento",
            description = "Aplica as transições permitidas pelo status atual; REALIZADO e CANCELADO são finais."
    )
    @ApiResponse(responseCode = "200", description = "Status alterado.")
    public EventoResponse transicionarStatus(
            @PathVariable Long id,
            @Valid @RequestBody TransicaoStatusRequest request
    ) {
        return eventoService.transicionarStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir evento", description = "Exclui um evento somente quando estiver em RASCUNHO.")
    @ApiResponse(responseCode = "204", description = "Evento excluído.")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        eventoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
