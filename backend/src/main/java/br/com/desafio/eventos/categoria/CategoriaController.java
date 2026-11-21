package br.com.desafio.eventos.categoria;

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
@RequestMapping("/api/eventos/{eventoId}/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(
            @PathVariable Long eventoId,
            @Valid @RequestBody CategoriaRequest request
    ) {
        CategoriaResponse categoria = categoriaService.criar(eventoId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{categoriaId}")
                .buildAndExpand(categoria.id())
                .toUri();
        return ResponseEntity.created(location).body(categoria);
    }

    @GetMapping
    public List<CategoriaResponse> listar(@PathVariable Long eventoId) {
        return categoriaService.listarPorEvento(eventoId);
    }

    @GetMapping("/{categoriaId}")
    public CategoriaResponse buscarPorId(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId
    ) {
        return categoriaService.buscarPorId(eventoId, categoriaId);
    }

    @PutMapping("/{categoriaId}")
    public CategoriaResponse atualizar(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId,
            @Valid @RequestBody CategoriaRequest request
    ) {
        return categoriaService.atualizar(eventoId, categoriaId, request);
    }

    @DeleteMapping("/{categoriaId}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId
    ) {
        categoriaService.excluir(eventoId, categoriaId);
        return ResponseEntity.noContent().build();
    }
}
