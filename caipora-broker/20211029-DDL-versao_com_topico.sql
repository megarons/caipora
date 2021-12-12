-- eventos.eventos_grupo definition

-- Drop table

-- DROP TABLE eventos.eventos_grupo;

CREATE TABLE eventos.eventos_grupo (
	topico varchar NOT NULL,
	nome_grupo varchar NOT NULL,
	particao int8 NOT NULL,
	ts_inclusao_registro timestamp NOT NULL,
	ts_fim_pendencia timestamp NULL,
	ts_ultima_atividade timestamp NULL,
	ts_finalizacao_registro timestamp NULL,
	estado_processamento varchar NOT NULL,
	ptl int8 NOT NULL,
	entrega int4 NULL,
	estado int4 NULL,
	tipologia int4 NULL,
	id_evento_offset int8 NOT NULL,
	CONSTRAINT eventos_grupo_pk PRIMARY KEY (nome_grupo, id_evento_offset)
);
CREATE INDEX eventos_grupo_topico_idx ON eventos.eventos_grupo USING btree (topico, nome_grupo, particao);


-- eventos.eventos_mensagens definition

-- Drop table

-- DROP TABLE eventos.eventos_mensagens;

CREATE TABLE eventos.eventos_mensagens (
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


-- eventos.subscricoes definition

-- Drop table

-- DROP TABLE eventos.subscricoes;

CREATE TABLE eventos.subscricoes (
	topico varchar NOT NULL,
	nome_grupo varchar NOT NULL,
	id_executor varchar NOT NULL,
	ts_ultima_atividade timestamp NOT NULL,
	entrega int4 NULL,
	estado int4 NULL,
	tipologia int4 NULL,
	CONSTRAINT subscricoes_pk PRIMARY KEY (topico, nome_grupo, id_executor)
);


-- eventos.subscricoes_particoes definition

-- Drop table

-- DROP TABLE eventos.subscricoes_particoes;

CREATE TABLE eventos.subscricoes_particoes (
	topico varchar NOT NULL,
	nome_grupo varchar NOT NULL,
	id_executor varchar NOT NULL,
	particao int4 NOT NULL
);