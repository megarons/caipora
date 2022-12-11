package br.com.caipora.eventos.v1.models;

public class ConfiguracaoConsumidorSubscrito {

	// campos de identificação 
	private String idGrupo;
	private String idExecutor;
	
	public ConfiguracaoConsumidorSubscrito() {
		super();
	}

	public ConfiguracaoConsumidorSubscrito(String idGrupo, String idExecutor) {
		super();
		this.idGrupo = idGrupo;
		this.idExecutor = idExecutor;
	}
	
	public String getIdGrupo() {
		return idGrupo;
	}
	public void setIdGrupo(String idGrupo) {
		this.idGrupo = idGrupo;
	}
	public String getIdExecutor() {
		return idExecutor;
	}
	public void setIdExecutor(String idExecutor) {
		this.idExecutor = idExecutor;
	}

    @Override
    public String toString() {
        return "ConfiguracaoConsumidorSubscrito [idGrupo=" + idGrupo + ", idExecutor=" + idExecutor + "]";
    }

}
