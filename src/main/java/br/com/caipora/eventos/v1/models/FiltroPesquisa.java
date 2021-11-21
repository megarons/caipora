package br.com.caipora.eventos.v1.models;

public class FiltroPesquisa {

	private int entrega;
	private int estado;
	private int tipologia;
	
	
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
	@Override
	public String toString() {
		return "FiltroPesquisa [entrega=" + entrega + ", estado=" + estado + ", tipologia=" + tipologia + "]";
	}
	
}
