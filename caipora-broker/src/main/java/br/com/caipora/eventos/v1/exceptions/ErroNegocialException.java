package br.com.caipora.eventos.v1.exceptions;

public class ErroNegocialException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CaiporaErro caiporaErro;

	public ErroNegocialException(CaiporaErro caiporaErro) {
		super(caiporaErro.getMensagem());
		this.setCaiporaErro(caiporaErro);
	}

	public ErroNegocialException() {
		super();
	}

	public ErroNegocialException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ErroNegocialException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErroNegocialException(String message) {
		super(message);
	}

	public ErroNegocialException(Throwable cause) {
		super(cause);
	}

	public ErroNegocialException(CaiporaErro caiporaErro, Throwable cause) {
		super(cause);
		this.caiporaErro = caiporaErro;
		
	}

	public CaiporaErro getCaiporaErro() {
		return caiporaErro;
	}

	public void setCaiporaErro(CaiporaErro caiporaErro) {
		this.caiporaErro = caiporaErro;
	}

}
