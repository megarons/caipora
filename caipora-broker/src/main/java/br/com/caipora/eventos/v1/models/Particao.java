package br.com.caipora.eventos.v1.models;

public class Particao {

	private int particao;
	private int cd_grupo;
	private String id_executor;

	public Particao(int cd_grupo, String id_executor, int particao) {
		this.particao = particao;
		this.cd_grupo = cd_grupo;
		this.id_executor = id_executor;
	}

	public int getParticao() {
		return particao;
	}

	public void setParticao(int particao) {
		this.particao = particao;
	}

	@Override
	public String toString() {
		return "Particao [particao=" + particao + ", cd_grupo=" + cd_grupo + ", id_executor=" + id_executor + "]";
	}


	public String getId_executor() {
		return id_executor;
	}

	public void setId_executor(String id_executor) {
		this.id_executor = id_executor;
	}

}
