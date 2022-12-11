package br.com.caipora.eventos.v1.models;

public class Particao {

	private int particao;
	private String topicoGrupo;
	private String idConsumidor;

	public Particao(String cd_grupo, String id_executor, int particao) {
		this.particao = particao;
		this.topicoGrupo = cd_grupo;
		this.idConsumidor = id_executor;
	}

	public int getParticao() {
		return particao;
	}

	public void setParticao(int particao) {
		this.particao = particao;
	}

	public String getTopicoGrupo() {
		return topicoGrupo;
	}

	public void setTopicoGrupo(String grupo) {
		this.topicoGrupo = grupo;
	}

	public String getIdConsumidor() {
		return idConsumidor;
	}

	public void setIdConsumidor(String idConsumidor) {
		this.idConsumidor = idConsumidor;
	}

	@Override
	public String toString() {
		return "Particao [particao=" + particao + ", topicoGrupo=" + topicoGrupo + ", idConsumidor=" + idConsumidor
				+ "]";
	}

	

}
