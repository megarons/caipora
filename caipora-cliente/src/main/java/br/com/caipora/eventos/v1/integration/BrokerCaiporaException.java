package br.com.caipora.eventos.v1.integration;

public class BrokerCaiporaException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BrokerCaiporaException(String message) {
		super(String.format("%s", message));
	}
}
