package br.com.desafio.eventos.cobertura;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos/{eventoId}/cobertura")
@Tag(name = "Cobertura")
@ApiResponses({
        @ApiResponse(responseCode = "404", description = "Evento não encontrado."),
        @ApiResponse(responseCode = "500", description = "Erro inesperado.")
})
public class CoberturaController {

    private final CoberturaService coberturaService;

    public CoberturaController(CoberturaService coberturaService) {
        this.coberturaService = coberturaService;
    }

    @GetMapping
    @Operation(
            summary = "Consultar cobertura do evento",
            description = """
                    Retorna cobertura geral ponderada (obrigatórios peso 2, opcionais peso 1), cobertura por categoria,
                    itens obrigatórios pendentes, itens que faltam ordenados e conflitos entre restrições e alérgenos.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Cobertura calculada.")
    public CoberturaResponse buscarPorEvento(@PathVariable Long eventoId) {
        return coberturaService.buscarPorEvento(eventoId);
    }
}
