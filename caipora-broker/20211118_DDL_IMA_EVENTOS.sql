-- eventos.eventos_grupo definition

-- Drop table

-- DROP TABLE eventos.eventos_grupo;

CREATE TABLE eventos.eventos_grupo (
	cd_grupo int8 NOT NULL, -- Código identificador do grupo ao qual o evento pertence
	id_evento_offset int8 NOT NULL, -- Offset do evento ocorrido
	particao int8 NOT NULL, -- Partição lógica para prover capacidade de segregação de eventos entre os consumidores
	estado_processamento int4 NOT NULL, -- Identifica os estados de processamento do evento :¶1 - Pronto para consumo¶2 - Finalizado
	recorrencia int8 NULL, -- Campo que identifica quantas vezes o evento foi pego entre consumidores sem ser finalizado. Permite calcular anomalias no consumo de um evento especifico.
	ts_inclusao_registro timestamp NOT NULL, -- TS da inclusão do evento na fila para consumo dos consumidores. É gerado quando um consumidor gera a paginação de processamento do grupo ao retirar o evento da tabela principal e criar a representação de eventos do grupo
	ts_ultima_atualizacao timestamp NULL, -- TS da ultima atualização ocorrida no evento.
	ts_finalizacao_registro timestamp NULL, -- TS da finalização do evento em seu grupo de processamento.
	ptl int8 NOT NULL, -- Mensagem fixa sera generalizada
	entrega int4 NULL, -- Mensagem fixa sera generalizada
	estado int4 NULL, -- Mensagem fixa sera generalizada
	tipologia int4 NULL, -- Mensagem fixa sera generalizada
	CONSTRAINT eventos_grupo_pk PRIMARY KEY (cd_grupo, id_evento_offset)
);
CREATE INDEX eventos_grupo_topico_idx ON eventos.eventos_grupo USING btree (cd_grupo, particao);

-- Column comments

COMMENT ON COLUMN eventos.eventos_grupo.cd_grupo IS 'Código identificador do grupo ao qual o evento pertence';
COMMENT ON COLUMN eventos.eventos_grupo.id_evento_offset IS 'Offset do evento ocorrido';
COMMENT ON COLUMN eventos.eventos_grupo.particao IS 'Partição lógica para prover capacidade de segregação de eventos entre os consumidores';
COMMENT ON COLUMN eventos.eventos_grupo.estado_processamento IS 'Identifica os estados de processamento do evento :¶1 - Pronto para consumo¶2 - Finalizado';
COMMENT ON COLUMN eventos.eventos_grupo.recorrencia IS 'Campo que identifica quantas vezes o evento foi pego entre consumidores sem ser finalizado. Permite calcular anomalias no consumo de um evento especifico.';
COMMENT ON COLUMN eventos.eventos_grupo.ts_inclusao_registro IS 'TS da inclusão do evento na fila para consumo dos consumidores. É gerado quando um consumidor gera a paginação de processamento do grupo ao retirar o evento da tabela principal e criar a representação de eventos do grupo';
COMMENT ON COLUMN eventos.eventos_grupo.ts_ultima_atualizacao IS 'TS da ultima atualização ocorrida no evento.';
COMMENT ON COLUMN eventos.eventos_grupo.ts_finalizacao_registro IS 'TS da finalização do evento em seu grupo de processamento.';
COMMENT ON COLUMN eventos.eventos_grupo.ptl IS 'Mensagem fixa sera generalizada';
COMMENT ON COLUMN eventos.eventos_grupo.entrega IS 'Mensagem fixa sera generalizada';
COMMENT ON COLUMN eventos.eventos_grupo.estado IS 'Mensagem fixa sera generalizada';
COMMENT ON COLUMN eventos.eventos_grupo.tipologia IS 'Mensagem fixa sera generalizada';


-- eventos.eventos_mensagens definition

-- Drop table

-- DROP TABLE eventos.eventos_mensagens;

CREATE TABLE eventos.eventos_mensagens (
	id_evento_offset int8 NOT NULL, -- Offset do evento ocorrido
	particao int8 NOT NULL, -- Partição lógica para prover capacidade de segregação de eventos entre os consumidores
	ts_inclusao_registro timestamp NOT NULL, -- TS da inclusão do evento.
	ptl int8 NOT NULL,
	entrega int4 NOT NULL,
	estado int4 NOT NULL,
	tipologia int4 NOT NULL,
	CONSTRAINT eventos_mensagens_pk PRIMARY KEY (id_evento_offset, ts_inclusao_registro)
);
COMMENT ON TABLE eventos.eventos_mensagens IS 'Tabela Original dos eventos empostados no tópico. A partir dessa tabelas são geradas Camadas de eventos por grupo, tabela eventos_grupo,  onde cada grupo tem seu ciclo de vida segregado da execução dos eventos principais existentes nessa tabela.';

-- Column comments

COMMENT ON COLUMN eventos.eventos_mensagens.id_evento_offset IS 'Offset do evento ocorrido';
COMMENT ON COLUMN eventos.eventos_mensagens.particao IS 'Partição lógica para prover capacidade de segregação de eventos entre os consumidores';
COMMENT ON COLUMN eventos.eventos_mensagens.ts_inclusao_registro IS 'TS da inclusão do evento.';


-- eventos.historico_eventos_grupo definition

-- Drop table

-- DROP TABLE eventos.historico_eventos_grupo;

CREATE TABLE eventos.historico_eventos_grupo (
	cd_grupo int8 NOT NULL, -- Código identificador do grupo ao qual o evento pertence
	id_evento_offset int8 NOT NULL, -- Offset do evento ocorrido
	ts_atualizacao_evento timestamp NOT NULL, -- Ts da atualização ocorrida em um evento.
	particao int8 NOT NULL, -- Partição lógica para prover capacidade de segregação de eventos entre os consumidores
	estado_processamento int4 NOT NULL, -- Identifica os estados de processamento do evento :¶1 - Pronto para consumo¶2 - Finalizado
	recorrencia int8 NULL, -- Campo que identifica quantas vezes o evento foi pego entre consumidores sem ser finalizado. Permite calcular anomalias no consumo de um evento especifico.
	ts_inclusao_registro timestamp NULL, -- TS da inclusão do evento na fila para consumo dos consumidores. É gerado quando um consumidor gera a paginação de processamento do grupo ao retirar o evento da tabela principal e criar a representação de eventos do grupo
	ts_ultima_atualizacao timestamp NULL, -- TS da ultima atualização ocorrida no evento.
	ts_finalizacao_registro timestamp NULL, -- TS da finalização do evento em seu grupo de processamento.
	ptl int8 NOT NULL,
	entrega int4 NULL,
	estado int4 NULL,
	tipologia int4 NULL,
	CONSTRAINT historico_eventos_grupo_pk PRIMARY KEY (cd_grupo, id_evento_offset, ts_atualizacao_evento)
);

-- Column comments

COMMENT ON COLUMN eventos.historico_eventos_grupo.cd_grupo IS 'Código identificador do grupo ao qual o evento pertence';
COMMENT ON COLUMN eventos.historico_eventos_grupo.id_evento_offset IS 'Offset do evento ocorrido';
COMMENT ON COLUMN eventos.historico_eventos_grupo.ts_atualizacao_evento IS 'Ts da atualização ocorrida em um evento.';
COMMENT ON COLUMN eventos.historico_eventos_grupo.particao IS 'Partição lógica para prover capacidade de segregação de eventos entre os consumidores';
COMMENT ON COLUMN eventos.historico_eventos_grupo.estado_processamento IS 'Identifica os estados de processamento do evento :
1 - Pronto para consumo
2 - Finalizado';
COMMENT ON COLUMN eventos.historico_eventos_grupo.recorrencia IS 'Campo que identifica quantas vezes o evento foi pego entre consumidores sem ser finalizado. Permite calcular anomalias no consumo de um evento especifico.';
COMMENT ON COLUMN eventos.historico_eventos_grupo.ts_inclusao_registro IS 'TS da inclusão do evento na fila para consumo dos consumidores. É gerado quando um consumidor gera a paginação de processamento do grupo ao retirar o evento da tabela principal e criar a representação de eventos do grupo';
COMMENT ON COLUMN eventos.historico_eventos_grupo.ts_ultima_atualizacao IS 'TS da ultima atualização ocorrida no evento.';
COMMENT ON COLUMN eventos.historico_eventos_grupo.ts_finalizacao_registro IS 'TS da finalização do evento em seu grupo de processamento.';


-- eventos.subscricoes definition

-- Drop table

-- DROP TABLE eventos.subscricoes;

CREATE TABLE eventos.subscricoes (
	cd_grupo int8 NOT NULL, -- Código identificador do grupo ao qual o consumidor pertence.
	id_executor varchar NOT NULL, -- Código identificar Unico do consumidor. É um indice distribuido para distinguir consumidores de um mesmo grupo. Tem tamanho maximo e tipo baseado no uso de UUID.
	ts_ultima_atividade timestamp NOT NULL, -- TS da ultima atividade do connsumidor. Utillizado para calcular o rebalnceamento baseado nos consumidores ativos/inativos que interagem nessa tabela.
	CONSTRAINT subscricoes_pk PRIMARY KEY (cd_grupo, id_executor)
);

-- Column comments

COMMENT ON COLUMN eventos.subscricoes.cd_grupo IS 'Código identificador do grupo ao qual o consumidor pertence.';
COMMENT ON COLUMN eventos.subscricoes.id_executor IS 'Código identificar Unico do consumidor. É um indice distribuido para distinguir consumidores de um mesmo grupo. Tem tamanho maximo e tipo baseado no uso de UUID.';
COMMENT ON COLUMN eventos.subscricoes.ts_ultima_atividade IS 'TS da ultima atividade do connsumidor. Utillizado para calcular o rebalnceamento baseado nos consumidores ativos/inativos que interagem nessa tabela.';


-- eventos.subscricoes_particoes definition

-- Drop table

-- DROP TABLE eventos.subscricoes_particoes;

CREATE TABLE eventos.subscricoes_particoes (
	cd_grupo int8 NOT NULL, -- Código identificador do grupo ao qual o consumidor pertence.
	id_executor varchar NOT NULL, -- Código identificar Unico do consumidor. É um indice distribuido para distinguir consumidores de um mesmo grupo. Tem tamanho maximo e tipo baseado no uso de UUID.
	particao int4 NOT NULL -- Código partição que o consumidor é responsavel por processar
);
COMMENT ON TABLE eventos.subscricoes_particoes IS 'Tabela que vincula quais partições lógicas é de responsabilidade de qual consumidor.';

-- Column comments

COMMENT ON COLUMN eventos.subscricoes_particoes.cd_grupo IS 'Código identificador do grupo ao qual o consumidor pertence.';
COMMENT ON COLUMN eventos.subscricoes_particoes.id_executor IS 'Código identificar Unico do consumidor. É um indice distribuido para distinguir consumidores de um mesmo grupo. Tem tamanho maximo e tipo baseado no uso de UUID.';
COMMENT ON COLUMN eventos.subscricoes_particoes.particao IS 'Código partição que o consumidor é responsavel por processar';


-- eventos.tipo_grupo definition

-- Drop table

-- DROP TABLE eventos.tipo_grupo;

CREATE TABLE eventos.tipo_grupo (
	cd_grupo int4 NOT NULL, -- Código identificador do grupo ao qual o consumidor pertence.
	nome_grupo varchar NOT NULL, -- Nome do grupo
	tempo_limite_balanceamento_segundos int4 NOT NULL DEFAULT 5, -- Tempo de keepAlive usado para  determinar um ciclo de conexao ativa entre o broker e cada um de seus consumidores de um mesmo grupo.
	tamanho_paginacao int4 NOT NULL DEFAULT 10, -- Tamanho da paginacao que sera gerada a cada vez que o consumidor leader for carregar eventos por todos os consumidores.
	indicador_ativo bpchar(1) NOT NULL DEFAULT 'N'::bpchar, -- Indica se o grupo cadastrado esta ativo para ser utilizado.
	CONSTRAINT tipo_grupo_pk PRIMARY KEY (cd_grupo)
);
COMMENT ON TABLE eventos.tipo_grupo IS 'Cadastro de grupo para processamento no broker.';

-- Column comments

COMMENT ON COLUMN eventos.tipo_grupo.cd_grupo IS 'Código identificador do grupo ao qual o consumidor pertence.';
COMMENT ON COLUMN eventos.tipo_grupo.nome_grupo IS 'Nome do grupo';
COMMENT ON COLUMN eventos.tipo_grupo.tempo_limite_balanceamento_segundos IS 'Tempo de keepAlive usado para  determinar um ciclo de conexao ativa entre o broker e cada um de seus consumidores de um mesmo grupo.';
COMMENT ON COLUMN eventos.tipo_grupo.tamanho_paginacao IS 'Tamanho da paginacao que sera gerada a cada vez que o consumidor leader for carregar eventos por todos os consumidores.';
COMMENT ON COLUMN eventos.tipo_grupo.indicador_ativo IS 'Indica se o grupo cadastrado esta ativo para ser utilizado.';