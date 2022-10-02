package br.com.caipora.eventos.v1.models;

public class ConfiguracaoConsumidorSubscrito {

	// campos de identificação 
	private int idGrupo;
	private String idExecutor;
	
	//campos de configuração
	private int tempoKeepAliveBalanceamentoSegundos;
	
	// campos filtráveis
	private String jsonFilter;
	
	public ConfiguracaoConsumidorSubscrito() {
		super();
	}

	public ConfiguracaoConsumidorSubscrito(int idGrupo, String idExecutor, int tempoKeepAliveBalanceamentoSegundos) {
		super();
		this.idGrupo = idGrupo;
		this.idExecutor = idExecutor;
		this.tempoKeepAliveBalanceamentoSegundos = tempoKeepAliveBalanceamentoSegundos;
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

	@Override
	public String toString() {
		return "ConfiguracaoConsumidorSubscrito [idGrupo=" + idGrupo + ", idExecutor=" + idExecutor
				+ ", tempoKeepAliveBalanceamentoSegundos=" + tempoKeepAliveBalanceamentoSegundos + ", jsonFilter="
				+ jsonFilter + "]";
	}

	public String getJsonFilter() {
		return jsonFilter;
	}

	public void setJsonFilter(String jsonFilter) {
		this.jsonFilter = jsonFilter;
	}

}
