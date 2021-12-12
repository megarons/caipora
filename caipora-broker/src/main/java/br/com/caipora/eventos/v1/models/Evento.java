package br.com.caipora.eventos.v1.models;

import java.io.Serializable;

public class Evento implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7987440404367839764L;
	Long protocolo;
    int particao;
    int entrega;
    int estado;
    int tipologia;
    
    public Evento() { }

    public Evento(Long protocolo, int particao, int entrega, int estado, int tipologia) {
        this.protocolo = protocolo;
        this.particao = particao;
        this.entrega = entrega;
        this.estado = estado;
        this.tipologia = tipologia;
    }

    public Long getProtocolo() {
        return protocolo;
    }
    public void setProtocolo(Long protocolo) {
        this.protocolo = protocolo;
    }
    public int getParticao() {
        return particao;
    }
    public void setParticao(int particao) {
        this.particao = particao;
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
}
