package br.com.caipora.eventos.v1.models;

public class ConfiguracaoConsumidor {

	// campos de identificação 
	private int idGrupo;
	private String idExecutor;
	
	//campos de configuração
	private int tempoKeepAliveBalanceamentoSegundos;
	
	// campos filtráveis
	private int identificadorEntrega;
	private int estadoDocumento;
	private int tipologiaDocumento;

	
	
	
	public ConfiguracaoConsumidor() {
		super();
	}

	public ConfiguracaoConsumidor(int idGrupo, String idExecutor, int tempoKeepAliveBalanceamentoSegundos) {
		super();
		this.idGrupo = idGrupo;
		this.idExecutor = idExecutor;
		this.tempoKeepAliveBalanceamentoSegundos = tempoKeepAliveBalanceamentoSegundos;
	}
	
	public ConfiguracaoConsumidor tipologiaDocumento(int tipologiaDocumento) {
		this.tipologiaDocumento = tipologiaDocumento;
		return this;
	}
	
	public ConfiguracaoConsumidor estadoDocumento(int estadoDocumento) {
		this.estadoDocumento = estadoDocumento;
		return this;
	}
	
	public ConfiguracaoConsumidor identificadorEntrega(int idEntrega) {
		this.identificadorEntrega = idEntrega;
		return this;
	}
	


	@Override
	public String toString() {
		return "ConfiguracaoConsumidor [idGrupo=" + idGrupo + ", idExecutor=" + idExecutor
				+ ", tempoKeepAliveBalanceamentoSegundos=" + tempoKeepAliveBalanceamentoSegundos
				+ ", identificadorEntrega=" + identificadorEntrega + ", estadoDocumento=" + estadoDocumento
				+ ", tipologiaDocumento=" + tipologiaDocumento + "]";
	}

	public int getIdGrupo() {
		return idGrupo;
	}

	public void setIdGrupo(int idGrupo) {
		this.idGrupo = idGrupo;
	}

	public String getIdExecutor() {
		return idExecutor;
	}

	public void setIdExecutor(String idExecutor) {
		this.idExecutor = idExecutor;
	}

	public int getTempoKeepAliveBalanceamentoSegundos() {
		return tempoKeepAliveBalanceamentoSegundos;
	}

	public void setTempoKeepAliveBalanceamentoSegundos(int tempoKeepAliveBalanceamentoSegundos) {
		this.tempoKeepAliveBalanceamentoSegundos = tempoKeepAliveBalanceamentoSegundos;
	}

	public int getIdentificadorEntrega() {
		return identificadorEntrega;
	}

	public void setIdentificadorEntrega(int identificadorEntrega) {
		this.identificadorEntrega = identificadorEntrega;
	}

	public int getEstadoDocumento() {
		return estadoDocumento;
	}

	public void setEstadoDocumento(int estadoDocumento) {
		this.estadoDocumento = estadoDocumento;
	}

	public int getTipologiaDocumento() {
		return tipologiaDocumento;
	}

	public void setTipologiaDocumento(int tipologiaDocumento) {
		this.tipologiaDocumento = tipologiaDocumento;
	}


}
