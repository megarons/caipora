package br.com.caipora.eventos.v1.models;

import java.sql.Timestamp;

public class EventoComunicacao {

    Evento evento;
	
	//dados controle
	private long offsetEvento;
	private long sequencialBalanceamento;
	

    private String estadoProcessamento;
	private int recorrencia;
	private String hashIntegridade;
	private Timestamp timestampInclusaoRegistro;
	private Timestamp timestampUltimaAtualizacao;

    private int codigoRetornoProcessamento = 200;
    private String textoMensagemRetornoProcessamento = "Sucesso";

    private long fatorMod;

    private long particaoCalculada;

    private long particaoSubscricao;

	
	public EventoComunicacao() {
		super();
	}

	public EventoComunicacao(String topicoGrupo, long id_evento_offset, String estado_processamento, int recorrencia,
			Timestamp timestampInclusaoRegistro, Timestamp timestampUltimaAtualizacao, String payload, long sequencialBalanceamento, long fatorMod) {
		super();
		this.offsetEvento = id_evento_offset;
		this.estadoProcessamento = estado_processamento;
		this.recorrencia = recorrencia;
		this.timestampInclusaoRegistro = timestampInclusaoRegistro;
		this.timestampUltimaAtualizacao = timestampUltimaAtualizacao;
		this.sequencialBalanceamento = sequencialBalanceamento;
		this.fatorMod = fatorMod;
		this.particaoCalculada = sequencialBalanceamento % fatorMod;
		
		evento = new Evento(topicoGrupo,payload);
		
	}
	
	public EventoComunicacao(String topicoGrupo, long id_evento_offset, String estado_processamento, int recorrencia,
            Timestamp timestampInclusaoRegistro, Timestamp timestampUltimaAtualizacao, String payload, long sequencialBalanceamento, long fatorMod, long particaoSubscricao) {
        super();
        this.offsetEvento = id_evento_offset;
        this.estadoProcessamento = estado_processamento;
        this.recorrencia = recorrencia;
        this.timestampInclusaoRegistro = timestampInclusaoRegistro;
        this.timestampUltimaAtualizacao = timestampUltimaAtualizacao;
        this.sequencialBalanceamento = sequencialBalanceamento;
        this.fatorMod = fatorMod;
        this.particaoCalculada = sequencialBalanceamento % fatorMod;
        this.particaoSubscricao = particaoSubscricao;
        
        evento = new Evento(topicoGrupo,payload);
        
    }
	
   public EventoComunicacao(String topicoGrupo, long id_evento_offset, String estado_processamento, 
            Timestamp timestampInclusaoRegistro, Timestamp timestampUltimaAtualizacao, String payload) {
        super();
        this.offsetEvento = id_evento_offset;
        this.estadoProcessamento = estado_processamento;
        this.timestampInclusaoRegistro = timestampInclusaoRegistro;
        this.timestampUltimaAtualizacao = timestampUltimaAtualizacao;
        
        evento = new Evento(topicoGrupo,payload);
        
    }


	public EventoComunicacao(int codigoRetorno, String mensagemRetorno, String topicoGrupo, long id_evento_offset, int particao, String estado_processamento, int recorrencia,
			Timestamp timestampInclusaoRegistro, Timestamp timestampUltimaAtualizacao,String hashIntegridade, String payload) {
		super();
		this.codigoRetornoProcessamento = codigoRetorno;
		this.textoMensagemRetornoProcessamento = mensagemRetorno;
		
		this.offsetEvento = id_evento_offset;
		this.estadoProcessamento = estado_processamento;
		this.recorrencia = recorrencia;
		this.timestampInclusaoRegistro = timestampInclusaoRegistro;
		this.timestampUltimaAtualizacao = timestampUltimaAtualizacao;
		this.hashIntegridade = hashIntegridade;
		
	    evento = new Evento(topicoGrupo,payload);
	}


    public Evento getEvento() {
        return evento;
    }
    
    public long getSequencialBalanceamento() {
        return sequencialBalanceamento;
    }

    public void setSequencialBalanceamento(long sequencialBalanceamento) {
        this.sequencialBalanceamento = sequencialBalanceamento;
    }

	public long getOffsetEvento() {
		return offsetEvento;
	}

	public void setOffsetEvento(long offsetEvento) {
		this.offsetEvento = offsetEvento;
	}


	public String getEstadoProcessamento() {
		return estadoProcessamento;
	}

	public void setEstadoProcessamento(String estadoProcessamento) {
		this.estadoProcessamento = estadoProcessamento;
	}

	public int getRecorrencia() {
		return recorrencia;
	}

	public void setRecorrencia(int recorrencia) {
		this.recorrencia = recorrencia;
	}

	public Timestamp getTimestampInclusaoRegistro() {
		return timestampInclusaoRegistro;
	}

	public void setTimestampInclusaoRegistro(Timestamp timestampInclusaoRegistro) {
		this.timestampInclusaoRegistro = timestampInclusaoRegistro;
	}

	public Timestamp getTimestampUltimaAtualizacao() {
		return timestampUltimaAtualizacao;
	}

	public void setTimestampUltimaAtualizacao(Timestamp timestampUltimaAtualizacao) {
		this.timestampUltimaAtualizacao = timestampUltimaAtualizacao;
	}

	


	@Override
    public String toString() {
        return "EventoComunicacao [evento=" + evento + ", offsetEvento=" + offsetEvento + ", sequencialBalanceamento="
                + sequencialBalanceamento + ", estadoProcessamento=" + estadoProcessamento + ", recorrencia="
                + recorrencia + ", hashIntegridade=" + hashIntegridade + ", timestampInclusaoRegistro="
                + timestampInclusaoRegistro + ", timestampUltimaAtualizacao=" + timestampUltimaAtualizacao
                + ", codigoRetornoProcessamento=" + codigoRetornoProcessamento + ", textoMensagemRetornoProcessamento="
                + textoMensagemRetornoProcessamento + ", fatorMod=" + fatorMod + ", particaoCalculada="
                + particaoCalculada + ", particaoSubscricao=" + particaoSubscricao + "]";
    }

	public int getCodigoRetornoProcessamento() {
		return codigoRetornoProcessamento;
	}

	public void setCodigoRetornoProcessamento(int codigoRetornoProcessamento) {
		this.codigoRetornoProcessamento = codigoRetornoProcessamento;
	}

	public String getTextoMensagemRetornoProcessamento() {
		return textoMensagemRetornoProcessamento;
	}

	public void setTextoMensagemRetornoProcessamento(String textoMensagemRetornoProcessamento) {
		this.textoMensagemRetornoProcessamento = textoMensagemRetornoProcessamento;
	}



	public String getHashIntegridade() {
		return hashIntegridade;
	}

	public void setHashIntegridade(String hashIntegridade) {
		this.hashIntegridade = hashIntegridade;
	}

    public long getFatorMod() {
        return fatorMod;
    }

    public void setFatorMod(long fatorMod) {
        this.fatorMod = fatorMod;
    }

    public long getParticaoCalculada() {
        return particaoCalculada;
    }

    public void setParticaoCalculada(long particaoCalculada) {
        this.particaoCalculada = particaoCalculada;
    }

    public long getParticaoSubscricao() {
        return particaoSubscricao;
    }

    public void setParticaoSubscricao(long particaoSubscricao) {
        this.particaoSubscricao = particaoSubscricao;
    }

   


}
