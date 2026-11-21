package br.com.desafio.eventos.contribuicao;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/eventos/{eventoId}/contribuicoes")
@Tag(name = "Contribuições")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida."),
        @ApiResponse(responseCode = "404", description = "Evento, item, inscrição ou contribuição não encontrado."),
        @ApiResponse(responseCode = "409", description = "Evento fechado, prazo encerrado ou quantidade acima da necessidade."),
        @ApiResponse(responseCode = "500", description = "Erro inesperado.")
})
public class ContribuicaoController {

    private final ContribuicaoService contribuicaoService;

    public ContribuicaoController(ContribuicaoService contribuicaoService) {
        this.contribuicaoService = contribuicaoService;
    }

    @PostMapping
    @Operation(
            summary = "Criar contribuição",
            description = "Registra contribuição de pessoa inscrita em evento ABERTO antes do prazo, sem ultrapassar a necessidade."
    )
    @ApiResponse(responseCode = "201", description = "Contribuição criada.")
    public ResponseEntity<ContribuicaoResponse> criar(
            @PathVariable Long eventoId,
            @Valid @RequestBody ContribuicaoRequest request
    ) {
        ContribuicaoResponse contribuicao = contribuicaoService.criar(eventoId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{contribuicaoId}")
                .buildAndExpand(contribuicao.id())
                .toUri();
        return ResponseEntity.created(location).body(contribuicao);
    }

    @GetMapping
    @Operation(summary = "Listar contribuições", description = "Lista as contribuições registradas para o evento.")
    @ApiResponse(responseCode = "200", description = "Contribuições encontradas.")
    public List<ContribuicaoResponse> listar(@PathVariable Long eventoId) {
        return contribuicaoService.listarPorEvento(eventoId);
    }

    @PutMapping("/{contribuicaoId}")
    @Operation(
            summary = "Atualizar contribuição",
            description = "Altera contribuição somente em evento ABERTO antes do prazo e respeitando o limite do item."
    )
    @ApiResponse(responseCode = "200", description = "Contribuição atualizada.")
    public ContribuicaoResponse atualizar(
            @PathVariable Long eventoId,
            @PathVariable Long contribuicaoId,
            @Valid @RequestBody ContribuicaoRequest request
    ) {
        return contribuicaoService.atualizar(eventoId, contribuicaoId, request);
    }

    @DeleteMapping("/{contribuicaoId}")
    @Operation(
            summary = "Remover contribuição",
            description = "Remove contribuição somente em evento ABERTO antes do prazo."
    )
    @ApiResponse(responseCode = "204", description = "Contribuição removida.")
    public ResponseEntity<Void> excluir(
            @PathVariable Long eventoId,
            @PathVariable Long contribuicaoId
    ) {
        contribuicaoService.excluir(eventoId, contribuicaoId);
        return ResponseEntity.noContent().build();
    }
}
