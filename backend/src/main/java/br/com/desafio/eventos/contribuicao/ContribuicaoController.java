package br.com.desafio.eventos.contribuicao;

import jakarta.validation.Valid;
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
public class ContribuicaoController {

    private final ContribuicaoService contribuicaoService;

    public ContribuicaoController(ContribuicaoService contribuicaoService) {
        this.contribuicaoService = contribuicaoService;
    }

    @PostMapping
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
    public List<ContribuicaoResponse> listar(@PathVariable Long eventoId) {
        return contribuicaoService.listarPorEvento(eventoId);
    }

    @PutMapping("/{contribuicaoId}")
    public ContribuicaoResponse atualizar(
            @PathVariable Long eventoId,
            @PathVariable Long contribuicaoId,
            @Valid @RequestBody ContribuicaoRequest request
    ) {
        return contribuicaoService.atualizar(eventoId, contribuicaoId, request);
    }

    @DeleteMapping("/{contribuicaoId}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long eventoId,
            @PathVariable Long contribuicaoId
    ) {
        contribuicaoService.excluir(eventoId, contribuicaoId);
        return ResponseEntity.noContent().build();
    }
}
