package br.com.caipora.eventos.v1.exceptions;

import br.com.caipora.eventos.v1.models.EventoComunicacao;

public class ComunicaConsumidorWarningHeader extends EventoComunicacao{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ComunicaConsumidorWarningHeader(int code, String msg){
		super.setCodigoRetornoProcessamento(code);
		super.setTextoMensagemRetornoProcessamento(msg);
	}
		
}
