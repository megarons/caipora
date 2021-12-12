package br.com.caipora.eventos.v1.exceptions;

import br.com.caipora.eventos.v1.models.PayloadEventoCaipora;

public class ComunicaConsumidorHeader extends PayloadEventoCaipora{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ComunicaConsumidorHeader(int code, String msg){
		super.setCodigoRetorno(code);
		super.setMensagemRetorno(msg);
	}
		
}
