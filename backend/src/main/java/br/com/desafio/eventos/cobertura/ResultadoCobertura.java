package br.com.desafio.eventos.cobertura;

import java.util.List;

public record ResultadoCobertura(
        double coberturaGeral,
        List<CategoriaCoberturaCalculada> categorias,
        List<ItemCoberturaCalculada> itens,
        List<ItemCoberturaCalculada> itensObrigatoriosPendentes,
        List<ItemCoberturaCalculada> oQueFalta
) {
}
