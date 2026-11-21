package br.com.desafio.eventos.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Organização de Eventos",
                version = "1.0.0",
                description = """
                        API para organizar eventos de alimentação, participantes, itens e contribuições.
                        A cobertura é informada como uma fração entre 0 e 1. Categorias podem ter meta
                        definida ou usar a soma das quantidades necessárias dos itens como base.
                        """,
                contact = @Contact(name = "Desafio técnico")
        ),
        tags = {
                @Tag(name = "Eventos", description = "Cadastro, consulta e transição de status de eventos."),
                @Tag(name = "Colaboradores", description = "Cadastro e manutenção de colaboradores."),
                @Tag(name = "Inscrições", description = "Inscrição e cancelamento de participantes em eventos."),
                @Tag(name = "Categorias", description = "Categorias e metas de cada evento."),
                @Tag(name = "Itens", description = "Itens necessários, obrigatoriedade e alérgenos."),
                @Tag(name = "Contribuições", description = "Contribuições dos participantes para os itens."),
                @Tag(name = "Cobertura", description = "Cobertura, pendências e conflitos alimentares do evento.")
        }
)
public class OpenApiConfiguration {
}
