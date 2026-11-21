package br.com.desafio.eventos.participante;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Inscrições")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida."),
        @ApiResponse(responseCode = "404", description = "Evento, colaborador ou inscrição não encontrado."),
        @ApiResponse(responseCode = "409", description = "Inscrição duplicada ou operação não permitida."),
        @ApiResponse(responseCode = "500", description = "Erro inesperado.")
})
public class InscricaoEventoController {

    private final InscricaoEventoService inscricaoEventoService;

    public InscricaoEventoController(InscricaoEventoService inscricaoEventoService) {
        this.inscricaoEventoService = inscricaoEventoService;
    }

    @PostMapping
    @Operation(
            summary = "Inscrever colaborador",
            description = "Inscreve um colaborador em evento ABERTO antes do prazo; cada colaborador participa uma vez por evento."
    )
    @ApiResponse(responseCode = "201", description = "Inscrição criada.")
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
    @Operation(summary = "Listar inscrições", description = "Lista os participantes inscritos no evento.")
    @ApiResponse(responseCode = "200", description = "Inscrições encontradas.")
    public List<InscricaoEventoResponse> listar(@PathVariable Long eventoId) {
        return inscricaoEventoService.listarPorEvento(eventoId);
    }

    @DeleteMapping("/{inscricaoId}")
    @Operation(
            summary = "Cancelar inscrição",
            description = "Cancela inscrição antes do prazo em evento ABERTO, desde que não tenha contribuições."
    )
    @ApiResponse(responseCode = "204", description = "Inscrição cancelada.")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long eventoId,
            @PathVariable Long inscricaoId
    ) {
        inscricaoEventoService.cancelarInscricao(eventoId, inscricaoId);
        return ResponseEntity.noContent().build();
    }
}
