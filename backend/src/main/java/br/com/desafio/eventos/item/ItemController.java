package br.com.desafio.eventos.item;

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
@RequestMapping("/api/eventos/{eventoId}/categorias/{categoriaId}/itens")
@Tag(name = "Itens")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida."),
        @ApiResponse(responseCode = "404", description = "Evento, categoria ou item não encontrado."),
        @ApiResponse(responseCode = "409", description = "Operação não permitida ou quantidade inferior ao total contribuído."),
        @ApiResponse(responseCode = "500", description = "Erro inesperado.")
})
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @Operation(
            summary = "Criar item",
            description = "Define quantidade necessária, obrigatoriedade e alérgenos do item."
    )
    @ApiResponse(responseCode = "201", description = "Item criado.")
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
    @Operation(summary = "Listar itens", description = "Lista os itens associados à categoria.")
    @ApiResponse(responseCode = "200", description = "Itens encontrados.")
    public List<ItemResponse> listar(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId
    ) {
        return itemService.listarPorCategoria(eventoId, categoriaId);
    }

    @GetMapping("/{itemId}")
    @Operation(summary = "Buscar item", description = "Retorna os dados de um item da categoria.")
    @ApiResponse(responseCode = "200", description = "Item encontrado.")
    public ItemResponse buscarPorId(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId,
            @PathVariable Long itemId
    ) {
        return itemService.buscarPorId(eventoId, categoriaId, itemId);
    }

    @PutMapping("/{itemId}")
    @Operation(
            summary = "Atualizar item",
            description = "Atualiza o item conforme o estado do evento; não reduz a quantidade abaixo do total contribuído."
    )
    @ApiResponse(responseCode = "200", description = "Item atualizado.")
    public ItemResponse atualizar(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemRequest request
    ) {
        return itemService.atualizar(eventoId, categoriaId, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Excluir item", description = "Exclui um item somente se não houver contribuições associadas.")
    @ApiResponse(responseCode = "204", description = "Item excluído.")
    public ResponseEntity<Void> excluir(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId,
            @PathVariable Long itemId
    ) {
        itemService.excluir(eventoId, categoriaId, itemId);
        return ResponseEntity.noContent().build();
    }
}
