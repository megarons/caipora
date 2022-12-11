package br.com.caipora.eventos.v1.models;

import java.sql.Timestamp;
import java.util.List;

public class Subscricao {

	private String stringDeBind;
	private String idConsumidor;
	private List<Particao> particao;
	private Timestamp tsUltimaAtividade;

	public Subscricao(String stringDeBind, String id_executor, List<Particao> particao, Timestamp ts_ultima_atividade) {
		super();
		this.stringDeBind = stringDeBind;
		this.idConsumidor = id_executor;
		this.particao = particao;
		this.tsUltimaAtividade = ts_ultima_atividade;
	}

	public String getStringDeBind() {
		return stringDeBind;
	}

	public void setStringDeBind(String stringDeBind) {
		this.stringDeBind = stringDeBind;
	}

	public String getIdConsumidor() {
		return idConsumidor;
	}

	public void setIdConsumidor(String idConsumidor) {
		this.idConsumidor = idConsumidor;
	}

	public List<Particao> getParticao() {
		return particao;
	}

	public void setParticao(List<Particao> particao) {
		this.particao = particao;
	}

	public Timestamp getTsUltimaAtividade() {
		return tsUltimaAtividade;
	}

	public void setTsUltimaAtividade(Timestamp tsUltimaAtividade) {
		this.tsUltimaAtividade = tsUltimaAtividade;
	}

	@Override
	public String toString() {
		return "Subscricao [stringDeBind=" + stringDeBind + ", idConsumidor=" + ((idConsumidor != null)?idConsumidor.trim():null )+ ", particao=" + particao
				+ ", tsUltimaAtividade=" + tsUltimaAtividade + "]";
	}


}
