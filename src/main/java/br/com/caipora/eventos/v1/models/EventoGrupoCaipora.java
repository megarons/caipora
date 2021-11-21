package br.com.caipora.eventos.v1.models;

import java.sql.Timestamp;


public class EventoGrupoCaipora {
	
	private int codigoRetorno = 200; 
	private String mensagemRetorno = "Novo Evento Encontrado";

	private int idGrupo;
	private long id_evento_offset;
	
	private int particao;
	private int estado_processamento;
	private int recorrencia;
	private Timestamp timestampInclusaoRegistro;
	private Timestamp timestampUltimaAtualizacao;

	private long ptl;
	private int entrega;
	private int estado;
	private int tipologia;

	public EventoGrupoCaipora() {
		super();
	}
	public EventoGrupoCaipora(int idGrupo, long id_evento_offset, int particao, int estado_processamento, int recorrencia, Timestamp timestampInclusaoRegistro, long ptl,  
			int entrega, int estado, int tipologia) {
		
		super();
		this.idGrupo = idGrupo;
		this.id_evento_offset = id_evento_offset;
		this.particao = particao;
		this.estado_processamento = estado_processamento;
		this.timestampInclusaoRegistro = timestampInclusaoRegistro;
		this.ptl = ptl;
		this.entrega = entrega;
		this.estado = estado;
		this.tipologia = tipologia;
		this.recorrencia = recorrencia;
	}
	
	public EventoGrupoCaipora(int idGrupo, long ptl, int particao, long id_evento_offset,
			int entrega, int estado, int tipologia, int estado_processamento, Timestamp timestampInclusaoRegistro) {
		
		super();
		this.idGrupo = idGrupo;
		this.particao = particao;
		this.id_evento_offset = id_evento_offset;
		this.estado_processamento = estado_processamento;
		this.entrega = entrega;
		this.estado = estado;
		this.tipologia = tipologia;
		this.ptl = ptl;
		this.timestampInclusaoRegistro = timestampInclusaoRegistro;
		
		
	}


	
	public EventoGrupoCaipora(int idGrupo, long id_evento_offset, int particao, int estado_processamento, int recorrencia,
			Timestamp timestampInclusaoRegistro, Timestamp timestampUltimaAtualizacao, long ptl, int entrega,
			int estado, int tipologia) {
		super();
		this.idGrupo = idGrupo;
		this.id_evento_offset = id_evento_offset;
		this.particao = particao;
		this.estado_processamento = estado_processamento;
		this.recorrencia = recorrencia;
		this.timestampInclusaoRegistro = timestampInclusaoRegistro;
		this.timestampUltimaAtualizacao = timestampUltimaAtualizacao;
		this.ptl = ptl;
		this.entrega = entrega;
		this.estado = estado;
		this.tipologia = tipologia;
	}
	public int getIdGrupo() {
		return idGrupo;
	}
	public void setIdGrupo(int idGrupo) {
		this.idGrupo = idGrupo;
	}
	public int getParticao() {
		return particao;
	}
	public void setParticao(int particao) {
		this.particao = particao;
	}
	public long getId_evento_offset() {
		return id_evento_offset;
	}
	public void setId_evento_offset(long id_evento_offset) {
		this.id_evento_offset = id_evento_offset;
	}

	public int getEntrega() {
		return entrega;
	}
	public void setEntrega(int entrega) {
		this.entrega = entrega;
	}
	public int getEstado() {
		return estado;
	}
	public void setEstado(int estado) {
		this.estado = estado;
	}
	public int getTipologia() {
		return tipologia;
	}
	public void setTipologia(int tipologia) {
		this.tipologia = tipologia;
	}
	public long getPtl() {
		return ptl;
	}
	public void setPtl(long ptl) {
		this.ptl = ptl;
	}
	@Override
	public String toString() {
		return "EventoGrupoCaipora [codigoRetorno=" + codigoRetorno + ", mensagemRetorno=" + mensagemRetorno + ", idGrupo="
				+ idGrupo + ", id_evento_offset=" + id_evento_offset + ", particao=" + particao
				+ ", estado_processamento=" + estado_processamento + ", recorrencia=" + recorrencia
				+ ", timestampInclusaoRegistro=" + timestampInclusaoRegistro + ", timestampUltimaAtualizacao="
				+ timestampUltimaAtualizacao + ", ptl=" + ptl + ", entrega=" + entrega + ", estado=" + estado
				+ ", tipologia=" + tipologia + "]";
	}
	public int getCodigoRetorno() {
		return codigoRetorno;
	}
	public void setCodigoRetorno(int codigoRetorno) {
		this.codigoRetorno = codigoRetorno;
	}
	public String getMensagemRetorno() {
		return mensagemRetorno;
	}
	public void setMensagemRetorno(String mensagemRetorno) {
		this.mensagemRetorno = mensagemRetorno;
	}
	public Timestamp getTimestampInclusaoRegistro() {
		return timestampInclusaoRegistro;
	}
	public void setTimestampInclusaoRegistro(Timestamp timestampInclusaoRegistro) {
		this.timestampInclusaoRegistro = timestampInclusaoRegistro;
	}

	public int getRecorrencia() {
		return recorrencia;
	}
	public void setRecorrencia(int recorrencia) {
		this.recorrencia = recorrencia;
	}
	public Timestamp getTimestampUltimaAtualizacao() {
		return timestampUltimaAtualizacao;
	}
	public void setTimestampUltimaAtualizacao(Timestamp timestampUltimaAtualizacao) {
		this.timestampUltimaAtualizacao = timestampUltimaAtualizacao;
	}
	public int getEstado_processamento() {
		return estado_processamento;
	}
	public void setEstado_processamento(int estado_processamento) {
		this.estado_processamento = estado_processamento;
	}
}
