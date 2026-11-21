package br.com.desafio.eventos.cobertura;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos/{eventoId}/cobertura")
public class CoberturaController {

    private final CoberturaService coberturaService;

    public CoberturaController(CoberturaService coberturaService) {
        this.coberturaService = coberturaService;
    }

    @GetMapping
    public CoberturaResponse buscarPorEvento(@PathVariable Long eventoId) {
        return coberturaService.buscarPorEvento(eventoId);
    }
}
