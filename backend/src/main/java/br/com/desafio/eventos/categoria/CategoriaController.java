package br.com.desafio.eventos.categoria;

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
@RequestMapping("/api/eventos/{eventoId}/categorias")
@Tag(name = "Categorias")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida."),
        @ApiResponse(responseCode = "404", description = "Evento ou categoria não encontrado."),
        @ApiResponse(responseCode = "409", description = "Operação não permitida para o estado atual do evento."),
        @ApiResponse(responseCode = "500", description = "Erro inesperado.")
})
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    @Operation(
            summary = "Criar categoria",
            description = "Cria uma categoria no evento enquanto ele permite alterações, antes do prazo."
    )
    @ApiResponse(responseCode = "201", description = "Categoria criada.")
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
    @Operation(summary = "Listar categorias", description = "Lista as categorias e metas definidas para o evento.")
    @ApiResponse(responseCode = "200", description = "Categorias encontradas.")
    public List<CategoriaResponse> listar(@PathVariable Long eventoId) {
        return categoriaService.listarPorEvento(eventoId);
    }

    @GetMapping("/{categoriaId}")
    @Operation(summary = "Buscar categoria", description = "Retorna uma categoria do evento.")
    @ApiResponse(responseCode = "200", description = "Categoria encontrada.")
    public CategoriaResponse buscarPorId(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId
    ) {
        return categoriaService.buscarPorId(eventoId, categoriaId);
    }

    @PutMapping("/{categoriaId}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza nome e meta da categoria conforme as regras do evento.")
    @ApiResponse(responseCode = "200", description = "Categoria atualizada.")
    public CategoriaResponse atualizar(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId,
            @Valid @RequestBody CategoriaRequest request
    ) {
        return categoriaService.atualizar(eventoId, categoriaId, request);
    }

    @DeleteMapping("/{categoriaId}")
    @Operation(summary = "Excluir categoria", description = "Exclui categoria somente se não possuir itens.")
    @ApiResponse(responseCode = "204", description = "Categoria excluída.")
    public ResponseEntity<Void> excluir(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId
    ) {
        categoriaService.excluir(eventoId, categoriaId);
        return ResponseEntity.noContent().build();
    }
}
