
CREATE SCHEMA IF NOT EXISTS "eventos";

USE "eventos";

CREATE TABLE IF NOT EXISTS conf_eventos (
	max_particoes int4 NOT NULL,
	max_conversacoes_ativas int4 NOT NULL
);

CREATE TABLE IF NOT EXISTS eventos_grupo (
	topico varchar NOT NULL,
	nome_grupo varchar NOT NULL,
	particao int8 NOT NULL,
	id_evento_offset int8 NOT NULL,
	ts_inclusao_registro timestamp NOT NULL,
	ts_fim_pendencia timestamp NULL,
	ts_ultima_atividade timestamp NULL,
	ts_finalizacao_registro timestamp NULL,
	estado_processamento varchar NOT NULL,
	ptl int8 NOT NULL,
	entrega int4 NULL,
	estado int4 NULL,
	tipologia int4 NULL,
	CONSTRAINT eventos_grupo_pk PRIMARY KEY (id_evento_offset, ts_inclusao_registro)
);

CREATE TABLE IF NOT EXISTS eventos_mensagens (
	topico varchar NOT NULL,
	id_evento_offset int8 NOT NULL,
	particao int8 NOT NULL,
	ts_inclusao_registro timestamp NOT NULL,
	ptl int8 NOT NULL,
	entrega int4 NOT NULL,
	estado int4 NOT NULL,
	tipologia int4 NOT NULL,
	CONSTRAINT eventos_mensagens_pk PRIMARY KEY (id_evento_offset, ts_inclusao_registro)
);

CREATE TABLE IF NOT EXISTS subscricoes (
	topico varchar NOT NULL,
	nome_grupo varchar NOT NULL,
	id_executor varchar NOT NULL,
	ts_ultima_atividade timestamp NOT NULL,
	entrega int4 NULL,
	estado int4 NULL,
	tipologia int4 NULL,
	CONSTRAINT subscricoes_pk PRIMARY KEY (topico, nome_grupo, id_executor)
);

CREATE TABLE IF NOT EXISTS subscricoes_particoes (
	topico varchar NOT NULL,
	nome_grupo varchar NOT NULL,
	id_executor varchar NOT NULL,
	particao int4 NOT NULL
);