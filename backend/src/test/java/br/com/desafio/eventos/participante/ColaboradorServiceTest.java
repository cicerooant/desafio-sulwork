package br.com.desafio.eventos.participante;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColaboradorServiceTest {

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @Mock
    private InscricaoEventoRepository inscricaoEventoRepository;

    @InjectMocks
    private ColaboradorService colaboradorService;

    @Test
    void deveCriarColaboradorComRestricoes() {
        when(colaboradorRepository.existsByCpf("52998224725")).thenReturn(false);
        when(colaboradorRepository.save(any(Colaborador.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ColaboradorResponse response = colaboradorService.criar(request("Joana", "52998224725"));

        assertEquals("Joana", response.nome());
        assertEquals(Set.of(RestricaoAlimentar.SEM_GLUTEN), response.restricoes());
    }

    @Test
    void deveRejeitarCpfJaCadastrado() {
        when(colaboradorRepository.existsByCpf("52998224725")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> colaboradorService.criar(request("Joana", "52998224725"))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(colaboradorRepository, never()).save(any(Colaborador.class));
    }

    private ColaboradorRequest request(String nome, String cpf) {
        return new ColaboradorRequest(
                nome,
                cpf,
                Set.of(RestricaoAlimentar.SEM_GLUTEN)
        );
    }
}
