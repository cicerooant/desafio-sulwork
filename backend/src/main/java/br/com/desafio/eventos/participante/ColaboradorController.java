package br.com.desafio.eventos.participante;

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
@RequestMapping("/api/colaboradores")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @PostMapping
    public ResponseEntity<ColaboradorResponse> criar(@Valid @RequestBody ColaboradorRequest request) {
        ColaboradorResponse colaborador = colaboradorService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(colaborador.id())
                .toUri();
        return ResponseEntity.created(location).body(colaborador);
    }

    @GetMapping
    public List<ColaboradorResponse> listar() {
        return colaboradorService.listar();
    }

    @GetMapping("/{id}")
    public ColaboradorResponse buscarPorId(@PathVariable Long id) {
        return colaboradorService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ColaboradorResponse atualizar(@PathVariable Long id, @Valid @RequestBody ColaboradorRequest request) {
        return colaboradorService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        colaboradorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
