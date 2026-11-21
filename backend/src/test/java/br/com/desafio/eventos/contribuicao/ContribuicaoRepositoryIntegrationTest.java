package br.com.desafio.eventos.contribuicao;

import br.com.desafio.eventos.cobertura.CoberturaCalculator;
import br.com.desafio.eventos.cobertura.CoberturaService;
import br.com.desafio.eventos.item.Alergeno;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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
@Import({CoberturaService.class, CoberturaCalculator.class})
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

    @Autowired
    private CoberturaService coberturaService;

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
        assertEquals(
                4,
                contribuicaoRepository.somarQuantidadePorItemExcluindoContribuicao(
                        itemId,
                        jdbcTemplate.queryForObject(
                                "SELECT MIN(id) FROM contribuicao WHERE item_id = ?",
                                Long.class,
                                itemId
                        )
                )
        );
    }

    @Test
    void deveCalcularCoberturaComMetaOpcionalEConflitosDosInscritos() {
        Long eventoId = inserirEvento();
        Long colaboradorId = inserirColaborador("Joana", "52998224725", "VEGANO");
        inserirColaboradorRestricao(colaboradorId, "SEM_GLUTEN");
        Long inscricaoId = inserirInscricao(eventoId, colaboradorId);
        Long categoriaComMeta = inserirCategoria(eventoId, "Lanches", 4);
        Long categoriaSemMeta = inserirCategoria(eventoId, "Bebidas", null);
        Long itemId = inserirItem(categoriaComMeta, "Pão", 4, true);
        inserirAlergeno(itemId, "GLUTEN");
        inserirAlergeno(itemId, "LEITE");
        Long itemSemMeta = inserirItem(categoriaSemMeta, "Suco", 6, false);
        inserirContribuicao(inscricaoId, itemId, 2);
        inserirContribuicao(inscricaoId, itemSemMeta, 3);

        var cobertura = coberturaService.buscarPorEvento(eventoId);

        assertEquals(0.5, cobertura.coberturaGeral(), 0.0001);
        assertEquals(4, cobertura.categorias().stream()
                .filter(categoria -> categoria.nome().equals("Lanches"))
                .findFirst()
                .orElseThrow()
                .metaQuantidade());
        var bebida = cobertura.categorias().stream()
                .filter(categoria -> categoria.nome().equals("Bebidas"))
                .findFirst()
                .orElseThrow();
        assertEquals(6, bebida.metaQuantidade());
        assertEquals(3, bebida.quantidadeFaltante());

        var pao = cobertura.itens().stream()
                .filter(item -> item.nomeItem().equals("Pão"))
                .findFirst()
                .orElseThrow();
        assertEquals(2, pao.conflitos().size());
        assertTrue(pao.conflitos().stream().anyMatch(conflito ->
                conflito.restricao().name().equals("SEM_GLUTEN")
                        && conflito.alergeno() == Alergeno.GLUTEN
                        && conflito.nivel().name().equals("ALTO")
        ));
        assertTrue(pao.conflitos().stream().anyMatch(conflito ->
                conflito.restricao().name().equals("VEGANO")
                        && conflito.alergeno() == Alergeno.LEITE
                        && conflito.nivel().name().equals("MEDIO")
        ));
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
                "ABERTO"
        );
    }

    private Long inserirColaborador(String nome, String cpf, String restricao) {
        Long colaboradorId = jdbcTemplate.queryForObject(
                "INSERT INTO colaborador (nome, cpf) VALUES (?, ?) RETURNING id",
                Long.class,
                nome,
                cpf
        );
        inserirColaboradorRestricao(colaboradorId, restricao);
        return colaboradorId;
    }

    private void inserirColaboradorRestricao(Long colaboradorId, String restricao) {
        jdbcTemplate.update(
                "INSERT INTO colaborador_restricao (colaborador_id, restricao) VALUES (?, ?)",
                colaboradorId,
                restricao
        );
    }

    private Long inserirInscricao(Long eventoId, Long colaboradorId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO inscricao_evento (evento_id, colaborador_id) VALUES (?, ?) RETURNING id",
                Long.class,
                eventoId,
                colaboradorId
        );
    }

    private Long inserirCategoria(Long eventoId, String nome, Integer meta) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO categoria (evento_id, nome, meta_quantidade) VALUES (?, ?, ?) RETURNING id",
                Long.class,
                eventoId,
                nome,
                meta
        );
    }

    private Long inserirItem(Long categoriaId, String nome, int quantidadeNecessaria, boolean obrigatorio) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO item_evento (categoria_id, nome, quantidade_necessaria, obrigatorio)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                categoriaId,
                nome,
                quantidadeNecessaria,
                obrigatorio
        );
    }

    private void inserirAlergeno(Long itemId, String alergeno) {
        jdbcTemplate.update(
                "INSERT INTO item_alergeno (item_id, alergeno) VALUES (?, ?)",
                itemId,
                alergeno
        );
    }

    private void inserirContribuicao(Long inscricaoId, Long itemId, int quantidade) {
        jdbcTemplate.update(
                "INSERT INTO contribuicao (inscricao_evento_id, item_id, quantidade) VALUES (?, ?, ?)",
                inscricaoId,
                itemId,
                quantidade
        );
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
