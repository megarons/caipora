package br.com.caipora.eventos.v1.exceptions;

public class CaiporaErro  {

	private String mensagem;
	private String codigo;

	public CaiporaErro(String codigo, String mensagem) {
		this.setCodigo(codigo);
		this.setMensagem(mensagem);
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	
	
}
