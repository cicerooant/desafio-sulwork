package br.com.desafio.eventos.erro;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void retornaProblemDetailParaValidacaoDeCampo() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/teste/validacao"))
                .andExpect(jsonPath("$.erros[0].campo").value("nome"));
    }

    @Test
    void retornaProblemDetailParaJsonMalformado() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("malformado")))
                .andExpect(jsonPath("$.instance").value("/teste/validacao"));
    }

    @Test
    void preservaStatusDasExcecoesDaAplicacao() throws Exception {
        mockMvc.perform(post("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Recurso não encontrado."));
    }

    @Test
    void retornaConflitoParaViolacaoDeIntegridade() throws Exception {
        mockMvc.perform(post("/teste/conflito"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail", containsString("integridade")));
    }

    @RestController
    @RequestMapping("/teste")
    static class ControllerDeTeste {

        @PostMapping("/validacao")
        void validar(@Valid @RequestBody Requisicao requisicao) {
        }

        @PostMapping("/nao-encontrado")
        void naoEncontrado() {
            throw new ResponseStatusException(NOT_FOUND, "Recurso não encontrado.");
        }

        @PostMapping("/conflito")
        void conflito() {
            throw new DataIntegrityViolationException("Não incluir esta mensagem na resposta.");
        }
    }

    record Requisicao(@NotBlank String nome) {
    }
}
