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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/colaboradores")
@Tag(name = "Colaboradores")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida."),
        @ApiResponse(responseCode = "404", description = "Colaborador não encontrado."),
        @ApiResponse(responseCode = "409", description = "CPF duplicado ou conflito com os dados."),
        @ApiResponse(responseCode = "500", description = "Erro inesperado.")
})
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @PostMapping
    @Operation(summary = "Criar colaborador", description = "Cadastra colaborador com CPF válido e restrições alimentares.")
    @ApiResponse(responseCode = "201", description = "Colaborador criado.")
    public ResponseEntity<ColaboradorResponse> criar(@Valid @RequestBody ColaboradorRequest request) {
        ColaboradorResponse colaborador = colaboradorService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(colaborador.id())
                .toUri();
        return ResponseEntity.created(location).body(colaborador);
    }

    @GetMapping
    @Operation(summary = "Listar colaboradores", description = "Retorna todos os colaboradores cadastrados.")
    @ApiResponse(responseCode = "200", description = "Colaboradores encontrados.")
    public List<ColaboradorResponse> listar() {
        return colaboradorService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar colaborador", description = "Retorna os dados de um colaborador pelo identificador.")
    @ApiResponse(responseCode = "200", description = "Colaborador encontrado.")
    public ColaboradorResponse buscarPorId(@PathVariable Long id) {
        return colaboradorService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar colaborador", description = "Atualiza nome, CPF e restrições alimentares.")
    @ApiResponse(responseCode = "200", description = "Colaborador atualizado.")
    public ColaboradorResponse atualizar(@PathVariable Long id, @Valid @RequestBody ColaboradorRequest request) {
        return colaboradorService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir colaborador", description = "Exclui o colaborador se não houver vínculos que impeçam a operação.")
    @ApiResponse(responseCode = "204", description = "Colaborador excluído.")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        colaboradorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
