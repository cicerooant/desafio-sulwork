package br.com.desafio.eventos.cobertura;

import java.util.List;

public record CoberturaResponse(
        double coberturaGeral,
        List<CategoriaCoberturaResponse> categorias,
        List<ItemCoberturaResponse> itens,
        List<ItemCoberturaResponse> itensObrigatoriosPendentes,
        List<ItemCoberturaResponse> oQueFalta
) {
}
