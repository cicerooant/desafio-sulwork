package br.com.desafio.eventos.participante;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @Test
    void deveAceitarCpfComDigitosVerificadoresValidos() {
        assertTrue(validator.isValid("52998224725", null));
    }

    @Test
    void deveRejeitarCpfComDigitoVerificadorIncorreto() {
        assertFalse(validator.isValid("52998224724", null));
    }

    @Test
    void deveRejeitarSequenciasRepetidas() {
        assertFalse(validator.isValid("00000000000", null));
        assertFalse(validator.isValid("11111111111", null));
    }

    @Test
    void deveRejeitarCpfComFormatoInvalido() {
        assertFalse(validator.isValid("5299822472", null));
        assertFalse(validator.isValid("529.982.247-25", null));
        assertFalse(validator.isValid(null, null));
    }
}
