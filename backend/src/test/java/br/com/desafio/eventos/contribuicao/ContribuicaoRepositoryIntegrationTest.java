package br.com.desafio.eventos.contribuicao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
class ContribuicaoRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("alimentacao")
            .withUsername("alimentacao")
            .withPassword("alimentacao");

    @Autowired
    private ContribuicaoRepository contribuicaoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void deveSomarContribuicoesDeUmItem() {
        Long itemId = criarItemComContribuicoes();

        assertEquals(7, contribuicaoRepository.somarQuantidadePorItem(itemId));
        assertTrue(contribuicaoRepository.existsByItemId(itemId));
    }

    private Long criarItemComContribuicoes() {
        Long eventoId = jdbcTemplate.queryForObject(
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
                "ABERTO"
        );
        Long colaboradorId = jdbcTemplate.queryForObject(
                "INSERT INTO colaborador (nome, cpf) VALUES (?, ?) RETURNING id",
                Long.class,
                "Joana",
                "52998224725"
        );
        Long inscricaoId = jdbcTemplate.queryForObject(
                "INSERT INTO inscricao_evento (evento_id, colaborador_id) VALUES (?, ?) RETURNING id",
                Long.class,
                eventoId,
                colaboradorId
        );
        Long categoriaId = jdbcTemplate.queryForObject(
                "INSERT INTO categoria (evento_id, nome, meta_quantidade) VALUES (?, ?, ?) RETURNING id",
                Long.class,
                eventoId,
                "Lanches",
                10
        );
        Long itemId = jdbcTemplate.queryForObject(
                """
                INSERT INTO item_evento (categoria_id, nome, quantidade_necessaria, obrigatorio)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                categoriaId,
                "Pão",
                10,
                true
        );
        jdbcTemplate.update(
                "INSERT INTO contribuicao (inscricao_evento_id, item_id, quantidade) VALUES (?, ?, ?)",
                inscricaoId,
                itemId,
                3
        );
        jdbcTemplate.update(
                "INSERT INTO contribuicao (inscricao_evento_id, item_id, quantidade) VALUES (?, ?, ?)",
                inscricaoId,
                itemId,
                4
        );
        return itemId;
    }
}
