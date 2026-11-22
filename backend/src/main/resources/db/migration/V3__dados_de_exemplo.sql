DO $$
DECLARE
    seed_evento_id BIGINT;
    categoria_lanches_id BIGINT;
    categoria_bebidas_id BIGINT;
    item_pao_id BIGINT;
    maria_id BIGINT;
    joao_id BIGINT;
    ana_id BIGINT;
    inscricao_joao_id BIGINT;
BEGIN
    SELECT id INTO seed_evento_id
    FROM evento
    WHERE nome = 'Almoço de boas-vindas'
    LIMIT 1;

    IF seed_evento_id IS NULL THEN
        INSERT INTO evento (nome, descricao, tipo, data_hora, prazo_alteracao, status)
        VALUES (
            'Almoço de boas-vindas',
            'Evento para integrar a equipe e combinar o almoço.',
            'ALMOCO',
            TIMESTAMP '2027-04-15 12:30:00',
            TIMESTAMP '2027-04-14 18:00:00',
            'ABERTO'
        )
        RETURNING id INTO seed_evento_id;
    END IF;

    INSERT INTO colaborador (nome, cpf)
    VALUES ('Maria Silva', '52998224725')
    ON CONFLICT (cpf) DO NOTHING;

    INSERT INTO colaborador (nome, cpf)
    VALUES ('João Souza', '16899535009')
    ON CONFLICT (cpf) DO NOTHING;

    INSERT INTO colaborador (nome, cpf)
    VALUES ('Ana Lima', '31691342000')
    ON CONFLICT (cpf) DO NOTHING;

    SELECT id INTO maria_id FROM colaborador WHERE cpf = '52998224725';
    SELECT id INTO joao_id FROM colaborador WHERE cpf = '16899535009';
    SELECT id INTO ana_id FROM colaborador WHERE cpf = '31691342000';

    INSERT INTO colaborador_restricao (colaborador_id, restricao)
    VALUES (maria_id, 'VEGANO'), (maria_id, 'SEM_GLUTEN')
    ON CONFLICT DO NOTHING;

    INSERT INTO colaborador_restricao (colaborador_id, restricao)
    VALUES (joao_id, 'SEM_LACTOSE')
    ON CONFLICT DO NOTHING;

    INSERT INTO colaborador_restricao (colaborador_id, restricao)
    VALUES (ana_id, 'SEM_AMENDOIM')
    ON CONFLICT DO NOTHING;

    SELECT id INTO categoria_lanches_id
    FROM categoria c
    WHERE c.evento_id = seed_evento_id AND c.nome = 'Lanches';

    IF categoria_lanches_id IS NULL THEN
        INSERT INTO categoria (evento_id, nome, meta_quantidade)
        VALUES (seed_evento_id, 'Lanches', 10)
        RETURNING id INTO categoria_lanches_id;
    END IF;

    SELECT id INTO categoria_bebidas_id
    FROM categoria c
    WHERE c.evento_id = seed_evento_id AND c.nome = 'Bebidas';

    IF categoria_bebidas_id IS NULL THEN
        INSERT INTO categoria (evento_id, nome, meta_quantidade)
        VALUES (seed_evento_id, 'Bebidas', NULL)
        RETURNING id INTO categoria_bebidas_id;
    END IF;

    SELECT id INTO item_pao_id
    FROM item_evento
    WHERE categoria_id = categoria_lanches_id AND nome = 'Pão';

    IF item_pao_id IS NULL THEN
        INSERT INTO item_evento (categoria_id, nome, quantidade_necessaria, obrigatorio)
        VALUES (categoria_lanches_id, 'Pão', 10, TRUE)
        RETURNING id INTO item_pao_id;

        INSERT INTO item_alergeno (item_id, alergeno)
        VALUES (item_pao_id, 'GLUTEN')
        ON CONFLICT DO NOTHING;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM item_evento
        WHERE categoria_id = categoria_bebidas_id AND nome = 'Suco'
    ) THEN
        INSERT INTO item_evento (categoria_id, nome, quantidade_necessaria, obrigatorio)
        VALUES (categoria_bebidas_id, 'Suco', 6, FALSE);
    END IF;

    INSERT INTO inscricao_evento (evento_id, colaborador_id)
    VALUES (seed_evento_id, maria_id), (seed_evento_id, joao_id), (seed_evento_id, ana_id)
    ON CONFLICT DO NOTHING;

    SELECT id INTO inscricao_joao_id
    FROM inscricao_evento i
    WHERE i.evento_id = seed_evento_id AND i.colaborador_id = joao_id;

    IF NOT EXISTS (
        SELECT 1 FROM contribuicao
        WHERE inscricao_evento_id = inscricao_joao_id AND item_id = item_pao_id
    ) THEN
        INSERT INTO contribuicao (inscricao_evento_id, item_id, quantidade)
        VALUES (inscricao_joao_id, item_pao_id, 4);
    END IF;
END $$;
