package br.com.desafio.eventos.participante;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColaboradorRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void deveAceitarRequestComCpfValido() {
        ColaboradorRequest request = new ColaboradorRequest(
                "Joana",
                "52998224725",
                Set.of(RestricaoAlimentar.SEM_GLUTEN)
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void deveRejeitarRequestComCpfInvalido() {
        ColaboradorRequest request = new ColaboradorRequest(
                "Joana",
                "52998224724",
                Set.of()
        );

        assertFalse(validator.validate(request).isEmpty());
    }
}
