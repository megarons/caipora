package br.com.caipora.eventos.v1.models;


public class EventoGrupoCaiporaBrokerComunication extends EventoGrupoCaipora{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EventoGrupoCaiporaBrokerComunication(int code, String msg){
		super.setCodigoRetorno(code);
		super.setMensagemRetorno(msg);
	}
		
}
