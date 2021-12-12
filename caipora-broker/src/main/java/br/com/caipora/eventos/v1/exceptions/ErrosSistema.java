package br.com.caipora.eventos.v1.exceptions;

public enum ErrosSistema implements IEnumErro {
	
	//erros fatais
    ERRO_SQL("900","Erro no sistema"),
    ERRO_SISTEMA_INDISPONIVEL("900","Broker de mensagens está indisponivel no momento."),
        
    //comunicacao ao consumidor
    CC_NAO_FORAM_ENCONTRADOS_REGISTROS("204","No content| Não foram encontrados registros p/ processar."),
    CC_REBALANCEANDO_CONSUMIDORES("503","Service Unavailable| Rebalanceado consumidores. Mantenha o parametro de keepAlive sempre <= ao tempo de ciclo de execucao e/ou ao tempo de ciclo do tratamento de erro do seu consumidor."), 
	
	//erros execucao
    ATINGIDO_LIMITE_MAXIMO_DE_CONSUMIDORES_CONCORRENTES("406","406 Not Acceptable| Foi atingido o limite máximo de consumidores para o grupo."),
	
    
    ERRO_AO_INSERIR_EVENTO("801","Erro ao inserir evento."), 
	ERRO_NEGOCIO_CODIGO_IDENTIFICADOR_GRUPO_NAO_INFORMADO("802","Código de grupo não informado."), 
	ERRO_NEGOCIO_CODIGO_IDENTIFICADOR_UNICO_CONSUMIDOR("803","Identificador unico do consumidor não informado."), 
	ERRO_NEGOCIO_KEEPALIVE_NAO_SETADO("804","ValorCicloTempoAtivo (KeepAlive) não informado."), 
	ERRO_NEGOCIO_OFFSET_NAO_INFORMADO("805","Offset do evento não informado."), 
	ERRO_NEGOCIO_PARTICAO_NAO_INFORMADA("806","Particao do evento não informada."), 
	ERRO_NEGOCIAL_GERA_HASH("807","Erro ao gerar Hash.");

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

