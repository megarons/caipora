package br.com.caipora.eventos.v1.models;

import java.io.Serializable;

public class Payload implements Serializable {
	

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	private static final long serialVersionUID = 1L;
	
	String payload;

	public Payload() {
	}

	public Payload(String payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "Evento [payload=" + payload + "]";
	}

	
}
