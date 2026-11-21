package br.com.desafio.eventos.cobertura;

import br.com.desafio.eventos.item.Alergeno;
import br.com.desafio.eventos.participante.RestricaoAlimentar;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConflitosAlimentaresTest {

    @Test
    void deveRetornarConflitoDiretoDeNivelAlto() {
        var conflitos = ConflitosAlimentares.encontrar(
                7L,
                "Ana",
                Set.of(RestricaoAlimentar.SEM_GLUTEN, RestricaoAlimentar.SEM_LEITE),
                Set.of(Alergeno.GLUTEN, Alergeno.LEITE)
        );

        assertEquals(2, conflitos.size());
        assertTrue(conflitos.stream().allMatch(conflito -> conflito.nivel() == NivelConflitoAlimentar.ALTO));
        assertTrue(conflitos.stream().allMatch(conflito -> conflito.colaboradorId().equals(7L)));
    }

    @Test
    void deveRetornarConflitoDeDietaDeNivelMedio() {
        var conflitos = ConflitosAlimentares.encontrar(
                8L,
                "Bia",
                Set.of(RestricaoAlimentar.VEGANO, RestricaoAlimentar.VEGETARIANO),
                Set.of(Alergeno.LEITE, Alergeno.PEIXE, Alergeno.CRUSTACEOS)
        );

        assertEquals(5, conflitos.size());
        assertTrue(conflitos.stream().allMatch(conflito -> conflito.nivel() == NivelConflitoAlimentar.MEDIO));
    }

    @Test
    void naoDeveRetornarConflitoSemCorrespondenciaNoMapa() {
        var conflitos = ConflitosAlimentares.encontrar(
                9L,
                "Caio",
                Set.of(RestricaoAlimentar.VEGANO),
                Set.of(Alergeno.GLUTEN, Alergeno.AMENDOIM)
        );

        assertTrue(conflitos.isEmpty());
    }
}
