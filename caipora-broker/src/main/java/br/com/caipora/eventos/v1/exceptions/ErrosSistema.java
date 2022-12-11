package br.com.caipora.eventos.v1.exceptions;

public enum ErrosSistema implements IEnumErro {
	
	//erros fatais
    ERRO_SQL("9999","Erro no sistema"),
    ERRO_SISTEMA_INDISPONIVEL("9999","Broker de mensagens está indisponivel no momento."),
        
    //comunicacao ao consumidor
    CC_NAO_FORAM_ENCONTRADOS_REGISTROS("1000","Não foram encontrados registros p/ processar."),
    CC_REBALANCEANDO_CONSUMIDORES("1001","Rebalanceado consumidores. Mantenha o parametro de keepAlive sempre <= ao tempo de ciclo de execucao e/ou ao tempo de ciclo do tratamento de erro do seu consumidor."), 
	
	//erros execucao
    ERRO_ATINGIDO_LIMITE_MAXIMO_DE_CONSUMIDORES_CONCORRENTES("9008","Não aceito| Foi atingido o limite máximo de consumidores para o grupo."),
	
    
    ERRO_AO_INSERIR_EVENTO("9000","Erro ao inserir evento."), 
	ERRO_NEGOCIO_CODIGO_IDENTIFICADOR_GRUPO_NAO_INFORMADO("9001","Código de grupo não informado."), 
	ERRO_NEGOCIO_CODIGO_IDENTIFICADOR_UNICO_CONSUMIDOR("9002","Identificador unico do consumidor não informado."), 
	ERRO_NEGOCIO_KEEPALIVE_NAO_SETADO("9003","ValorCicloTempoAtivo (KeepAlive) não informado."), 
	ERRO_NEGOCIO_OFFSET_NAO_INFORMADO("9004","Offset do evento não informado."), 
	ERRO_NEGOCIO_PARTICAO_NAOINFORMADA("9005","Particao do evento não informada."), 
	ERRO_NEGOCIAL_GERA_HASH("9006","Erro ao gerar Hash."),
	ERRO_NEGOCIAL_HASH_INTEGRIDADE_NAO_CONFERE("9007","Hash não confere. A mensagem não foi gerada pelo broker."),
	ERRO_NEGOCIAL_TOPICO_INATIVO("9008","Topico esta inativo. Ative o topico para inserir novos eventos.");

    String codigo;
    String mensagem;
    ErrosSistema(String codigo, String mensagem) {
        this.codigo = codigo;
        this.mensagem = mensagem;
    }

    @Override
    public CaiporaErro get() {
        return new CaiporaErro(codigo, mensagem);
    }
}

