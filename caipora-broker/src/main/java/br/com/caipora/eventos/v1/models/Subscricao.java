package br.com.caipora.eventos.v1.models;

import java.sql.Timestamp;
import java.util.List;

public class Subscricao {

	private int cd_grupo;
	private String id_executor;
	private List<Particao> particao;
	private Timestamp ts_ultima_atividade;

	public Timestamp getTs_ultima_atividade() {
		return ts_ultima_atividade;
	}

	public void setTs_ultima_atividade(Timestamp ts_ultima_atividade) {
		this.ts_ultima_atividade = ts_ultima_atividade;
	}

	public Subscricao(int cd_grupo, String id_executor, List<Particao> particao, Timestamp ts_ultima_atividade) {
		super();
		this.cd_grupo = cd_grupo;
		this.id_executor = id_executor;
		this.particao = particao;
		this.ts_ultima_atividade = ts_ultima_atividade;
	}

	@Override
	public String toString() {
		return "Subscricao [cd_grupo=" + cd_grupo + ", id_executor=" + id_executor + ", particao=" + particao
				+ ", ts_ultima_atividade=" + ts_ultima_atividade + "]";
	}

	public String getId_executor() {
		return id_executor;
	}

	public void setId_executor(String id_executor) {
		this.id_executor = id_executor;
	}

	public List<Particao> getParticao() {
		return particao;
	}

	public void setParticao(List<Particao> particao) {
		this.particao = particao;
	}

	public int getCd_grupo() {
		return cd_grupo;
	}

	public void setCd_grupo(int cd_grupo) {
		this.cd_grupo = cd_grupo;
	}

}
