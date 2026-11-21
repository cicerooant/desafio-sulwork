package br.com.desafio.eventos.evento;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusEventoTest {

    @Test
    void devePermitirSomenteAsTransicoesDefinidas() {
        assertTrue(StatusEvento.RASCUNHO.podeTransicionarPara(StatusEvento.ABERTO));
        assertTrue(StatusEvento.RASCUNHO.podeTransicionarPara(StatusEvento.CANCELADO));
        assertTrue(StatusEvento.ABERTO.podeTransicionarPara(StatusEvento.ENCERRADO));
        assertTrue(StatusEvento.ABERTO.podeTransicionarPara(StatusEvento.CANCELADO));
        assertTrue(StatusEvento.ENCERRADO.podeTransicionarPara(StatusEvento.REALIZADO));
        assertTrue(StatusEvento.ENCERRADO.podeTransicionarPara(StatusEvento.CANCELADO));

        assertFalse(StatusEvento.RASCUNHO.podeTransicionarPara(StatusEvento.ENCERRADO));
        assertFalse(StatusEvento.ABERTO.podeTransicionarPara(StatusEvento.REALIZADO));
        assertFalse(StatusEvento.REALIZADO.podeTransicionarPara(StatusEvento.CANCELADO));
        assertFalse(StatusEvento.CANCELADO.podeTransicionarPara(StatusEvento.RASCUNHO));
        assertFalse(StatusEvento.REALIZADO.podeTransicionarPara(StatusEvento.REALIZADO));
    }
}
