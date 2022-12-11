
CREATE SCHEMA IF NOT EXISTS "caipora";

USE "caipora";

-- caipora.conf_grupo_consumidor_topico definition

-- Drop table

DROP TABLE IF EXISTS caipora.conf_grupo_consumidor_topico;

CREATE TABLE caipora.conf_grupo_consumidor_topico (
	tx_id_topico_bind varchar NOT NULL, -- Código identificador do grupo ao qual o consumidor pertence.
	descricao_grupo_consumidor varchar NULL, -- Nome do grupo
	indicador_ativo bpchar(1) NOT NULL DEFAULT 'N'::bpchar, -- Indica se o grupo cadastrado esta ativo para ser utilizado.
	keep_alive_segundos int4 NOT NULL DEFAULT 5,
	tamanho_paginacao int4 NOT NULL DEFAULT 10, -- Quantidade registro para ser colocado em buffer de processamento por consumidor 
	quantidade_particao int4 NOT NULL DEFAULT 10, -- Quantidade maxima de processamento de consumidores em paralelo.
	CONSTRAINT cadastro_tipo_consumidor_topico_pk PRIMARY KEY (tx_id_topico_bind)
);
COMMENT ON TABLE caipora.conf_grupo_consumidor_topico IS 'Cadastro de tipo de consumidor de eventos.';

-- Column comments

COMMENT ON COLUMN caipora.conf_grupo_consumidor_topico.tx_id_topico_bind IS 'Código identificador do grupo ao qual o consumidor pertence.';
COMMENT ON COLUMN caipora.conf_grupo_consumidor_topico.descricao_grupo_consumidor IS 'Nome do grupo';
COMMENT ON COLUMN caipora.conf_grupo_consumidor_topico.indicador_ativo IS 'Indica se o grupo cadastrado esta ativo para ser utilizado.';
COMMENT ON COLUMN caipora.conf_grupo_consumidor_topico.tamanho_paginacao IS 'Quantidade registro para ser colocado em buffer de processamento por consumidor ';
COMMENT ON COLUMN caipora.conf_grupo_consumidor_topico.quantidade_particao IS 'Quantidade maxima de processamento de consumidores em paralelo.';


-- caipora.conf_topico definition

-- Drop table

DROP TABLE IF EXISTS caipora.conf_topico;

CREATE TABLE caipora.conf_topico (
	tx_id_topico varchar NOT NULL, -- Texto identificador do topico (um mesmo topico é o que define um grupo de consumidores)
	descricao_topico varchar NOT NULL, -- Texto descritivo do topico 
	indicador_ativo bpchar(1) NOT NULL DEFAULT 'S'::bpchar, -- Indica se o topico esta ativo ou não.¶A inativação de um tópico desabilita a inclusão de eventos nesse tópico.
	CONSTRAINT conf_topico_pk PRIMARY KEY (tx_id_topico)
);
COMMENT ON TABLE caipora.conf_topico IS 'Configuracao dos Topicos providos no broker';

-- Column comments

COMMENT ON COLUMN caipora.conf_topico.tx_id_topico IS 'Texto identificador do topico (um mesmo topico é o que define um grupo de consumidores)';
COMMENT ON COLUMN caipora.conf_topico.descricao_topico IS 'Texto descritivo do topico ';
COMMENT ON COLUMN caipora.conf_topico.indicador_ativo IS 'Indica se o topico esta ativo ou não.
A inativação de um tópico desabilita a inclusão de eventos nesse tópico.';


-- caipora.evento_consumidor_topico definition

-- Drop table

DROP TABLE IF EXISTS caipora.evento_consumidor_topico;

CREATE TABLE caipora.evento_consumidor_topico (
	tx_id_topico_bind varchar NOT NULL, -- Texto identificador do topico ao qual o evento pertence
	id_evento_offset int8 NOT NULL, -- Offset do evento ocorrido
	particao int4 NOT NULL, -- Partição lógica para prover capacidade de segregação de eventos entre os consumidores
	estado_processamento bpchar(1) NOT NULL, -- Identifica os estados de processamento do evento :¶1 - Pronto para consumo¶2 - Finalizado
	recorrencia int8 NULL, -- Campo que identifica quantas vezes o evento foi pego entre consumidores sem ser finalizado. Permite calcular anomalias no consumo de um evento especifico.
	ts_inclusao_registro_original timestamp NOT NULL,
	ts_inclusao_registro timestamp NOT NULL, -- TS da inclusão do evento na fila para consumo dos consumidores. É gerado quando um consumidor gera a paginação de processamento do grupo ao retirar o evento da tabela principal e criar a representação de eventos do grupo
	ts_ultima_atualizacao timestamp NULL, -- TS da ultima atualização ocorrida no evento.
	ts_finalizacao_registro timestamp NULL, -- TS da finalização do evento em seu grupo de processamento.
	payload varchar NOT NULL, -- Evento que se quer emitir/consumir
	CONSTRAINT evento_consumidor_topico_pk PRIMARY KEY (tx_id_topico_bind, id_evento_offset)
);
CREATE INDEX evento_consumidor_topico_idx ON caipora.evento_consumidor_topico USING btree (tx_id_topico_bind, particao);
COMMENT ON TABLE caipora.evento_consumidor_topico IS 'Tabela que representa os eventos de consumo de um grupo de mesmo topico. A definicao de filtro de topico eh o que define um grupo de consumidores. Por exemplo em um topico EVENTO_SEND_MAIL um grupo de consumidores que detiverem seu topico de bind = EVENTO_SEND_MAIL receberao eventos para processarem, consumidores com topico de bind EVENTO_* tb receberão os eventos desse topico';

-- Column comments

COMMENT ON COLUMN caipora.evento_consumidor_topico.tx_id_topico_bind IS 'Texto identificador do topico ao qual o evento pertence';
COMMENT ON COLUMN caipora.evento_consumidor_topico.id_evento_offset IS 'Offset do evento ocorrido';
COMMENT ON COLUMN caipora.evento_consumidor_topico.particao IS 'Partição lógica para prover capacidade de segregação de eventos entre os consumidores';
COMMENT ON COLUMN caipora.evento_consumidor_topico.estado_processamento IS 'Identifica os estados de processamento do evento :¶1 - Pronto para consumo¶2 - Finalizado';
COMMENT ON COLUMN caipora.evento_consumidor_topico.recorrencia IS 'Campo que identifica quantas vezes o evento foi pego entre consumidores sem ser finalizado. Permite calcular anomalias no consumo de um evento especifico.';
COMMENT ON COLUMN caipora.evento_consumidor_topico.ts_inclusao_registro IS 'TS da inclusão do evento na fila para consumo dos consumidores. É gerado quando um consumidor gera a paginação de processamento do grupo ao retirar o evento da tabela principal e criar a representação de eventos do grupo';
COMMENT ON COLUMN caipora.evento_consumidor_topico.ts_ultima_atualizacao IS 'TS da ultima atualização ocorrida no evento.';
COMMENT ON COLUMN caipora.evento_consumidor_topico.ts_finalizacao_registro IS 'TS da finalização do evento em seu grupo de processamento.';
COMMENT ON COLUMN caipora.evento_consumidor_topico.payload IS 'Evento que se quer emitir/consumir';


-- caipora.subscricoes definition

-- Drop table

DROP TABLE IF EXISTS caipora.subscricoes;

CREATE TABLE caipora.subscricoes (
	tx_id_topico_bind varchar NOT NULL, -- Texto identificador do topico ao qual o evento pertence
	id_executor varchar NOT NULL, -- Código identificar Unico do consumidor. É um indice distribuido para distinguir consumidores de um mesmo grupo. Tem tamanho maximo e tipo baseado no uso de UUID.
	ts_ultima_atividade timestamp NOT NULL, -- TS da ultima atividade do connsumidor. Utillizado para calcular o rebalnceamento baseado nos consumidores ativos/inativos que interagem nessa tabela.
	CONSTRAINT subscricoes_pk PRIMARY KEY (tx_id_topico_bind, id_executor)
);

-- Column comments

COMMENT ON COLUMN caipora.subscricoes.tx_id_topico_bind IS 'Texto identificador do topico ao qual o evento pertence';
COMMENT ON COLUMN caipora.subscricoes.id_executor IS 'Código identificar Unico do consumidor. É um indice distribuido para distinguir consumidores de um mesmo grupo. Tem tamanho maximo e tipo baseado no uso de UUID.';
COMMENT ON COLUMN caipora.subscricoes.ts_ultima_atividade IS 'TS da ultima atividade do connsumidor. Utillizado para calcular o rebalnceamento baseado nos consumidores ativos/inativos que interagem nessa tabela.';


-- caipora.subscricoes_particoes definition

-- Drop table

DROP TABLE IF EXISTS caipora.subscricoes_particoes;

CREATE TABLE caipora.subscricoes_particoes (
	tx_id_topico_bind varchar NOT NULL, -- Texto identificador do topico ao qual o evento pertence
	id_executor varchar NOT NULL, -- Código identificar Unico do consumidor. É um indice distribuido para distinguir consumidores de um mesmo grupo. Tem tamanho maximo e tipo baseado no uso de UUID.
	particao int4 NOT NULL -- Código partição que o consumidor é responsavel por processar
);
COMMENT ON TABLE caipora.subscricoes_particoes IS 'Tabela que vincula quais partições lógicas é de responsabilidade de qual consumidor.';

-- Column comments

COMMENT ON COLUMN caipora.subscricoes_particoes.tx_id_topico_bind IS 'Texto identificador do topico ao qual o evento pertence';
COMMENT ON COLUMN caipora.subscricoes_particoes.id_executor IS 'Código identificar Unico do consumidor. É um indice distribuido para distinguir consumidores de um mesmo grupo. Tem tamanho maximo e tipo baseado no uso de UUID.';
COMMENT ON COLUMN caipora.subscricoes_particoes.particao IS 'Código partição que o consumidor é responsavel por processar';


-- caipora.evento_original_topico definition

-- Drop table

DROP TABLE IF EXISTS caipora.evento_original_topico;

CREATE TABLE caipora.evento_original_topico (
	tx_id_topico varchar NOT NULL, -- Texto identificador do topico ao qual o evento pertence
	id_evento_offset int8 NOT NULL, -- Offset do evento ocorrido
	particao int4 NOT NULL, -- Partição lógica para prover capacidade de segregação de eventos entre os consumidores
	ts_inclusao_registro timestamp NOT NULL, -- TS da inclusão do evento.
	payload varchar NOT NULL, -- Evento objeto do processamento.
	CONSTRAINT evento_original_topico_pk PRIMARY KEY (tx_id_topico, id_evento_offset, ts_inclusao_registro),
	CONSTRAINT evento_original_topico_fk FOREIGN KEY (tx_id_topico) REFERENCES caipora.conf_topico(tx_id_topico)
);
COMMENT ON TABLE caipora.evento_original_topico IS 'Tabela Original dos eventos empostados no tópico. A partir dessa tabelas são geradas Camadas de eventos por topico, tabela evento_consumidor_topico,  onde cada topico filtrado tem seu ciclo de vida segregado da execução dos eventos principais existentes nessa tabela. Ou seja, eventos originais se mantem nessa tabela enquanto na tabela evento_consumidor_topico é mantido um historico para cada consumidor distinto do topico original.';

-- Column comments

COMMENT ON COLUMN caipora.evento_original_topico.tx_id_topico IS 'Texto identificador do topico ao qual o evento pertence';
COMMENT ON COLUMN caipora.evento_original_topico.id_evento_offset IS 'Offset do evento ocorrido';
COMMENT ON COLUMN caipora.evento_original_topico.particao IS 'Partição lógica para prover capacidade de segregação de eventos entre os consumidores';
COMMENT ON COLUMN caipora.evento_original_topico.ts_inclusao_registro IS 'TS da inclusão do evento.';
COMMENT ON COLUMN caipora.evento_original_topico.payload IS 'Evento objeto do processamento.';
