package br.com.caipora.eventos.v1.exceptions;

public class ClienteCaiporaEventosException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClienteCaiporaEventosException() {
		super();
	}

	public ClienteCaiporaEventosException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ClienteCaiporaEventosException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClienteCaiporaEventosException(String message) {
		super(message);
	}

	public ClienteCaiporaEventosException(Throwable cause) {
		super(cause);
	}

}
