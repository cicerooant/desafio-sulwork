package br.com.desafio.eventos.item;

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
@RequestMapping("/api/eventos/{eventoId}/categorias/{categoriaId}/itens")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> criar(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId,
            @Valid @RequestBody ItemRequest request
    ) {
        ItemResponse item = itemService.criar(eventoId, categoriaId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{itemId}")
                .buildAndExpand(item.id())
                .toUri();
        return ResponseEntity.created(location).body(item);
    }

    @GetMapping
    public List<ItemResponse> listar(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId
    ) {
        return itemService.listarPorCategoria(eventoId, categoriaId);
    }

    @GetMapping("/{itemId}")
    public ItemResponse buscarPorId(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId,
            @PathVariable Long itemId
    ) {
        return itemService.buscarPorId(eventoId, categoriaId, itemId);
    }

    @PutMapping("/{itemId}")
    public ItemResponse atualizar(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemRequest request
    ) {
        return itemService.atualizar(eventoId, categoriaId, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId,
            @PathVariable Long itemId
    ) {
        itemService.excluir(eventoId, categoriaId, itemId);
        return ResponseEntity.noContent().build();
    }
}
