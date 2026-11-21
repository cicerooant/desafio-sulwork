package br.com.desafio.eventos.participante;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Testcontainers
class InscricaoEventoRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("alimentacao")
            .withUsername("alimentacao")
            .withPassword("alimentacao");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void deveImpedirDuasInscricoesDoMesmoColaboradorNoMesmoEvento() {
        Long eventoId = inserirEvento();
        Long colaboradorId = inserirColaborador();

        inserirInscricao(eventoId, colaboradorId);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirInscricao(eventoId, colaboradorId)
        );
    }

    @Test
    void devePermitirInscreverMesmoColaboradorEmEventosDiferentes() {
        Long primeiroEventoId = inserirEvento();
        Long segundoEventoId = inserirEvento();
        Long colaboradorId = inserirColaborador();

        inserirInscricao(primeiroEventoId, colaboradorId);

        inserirInscricao(segundoEventoId, colaboradorId);
    }

    private Long inserirEvento() {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO evento (nome, tipo, data_hora, prazo_alteracao, status)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                "Almoço",
                "ALMOCO",
                LocalDateTime.of(2027, 5, 10, 12, 0),
                LocalDateTime.of(2027, 5, 9, 12, 0),
                "RASCUNHO"
        );
    }

    private Long inserirColaborador() {
        return jdbcTemplate.queryForObject(
                "INSERT INTO colaborador (nome, cpf) VALUES (?, ?) RETURNING id",
                Long.class,
                "Joana",
                "52998224725"
        );
    }

    private void inserirInscricao(Long eventoId, Long colaboradorId) {
        jdbcTemplate.update(
                "INSERT INTO inscricao_evento (evento_id, colaborador_id) VALUES (?, ?)",
                eventoId,
                colaboradorId
        );
    }
}
