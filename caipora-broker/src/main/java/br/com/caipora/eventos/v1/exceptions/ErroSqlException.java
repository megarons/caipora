package br.com.caipora.eventos.v1.exceptions;



import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Objects;

public class ErroSqlException extends ErroNegocialException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ErroSqlException(Exception e) throws ErroNegocialException{
        super(ErrosSistema.ERRO_SQL.get(), e.getCause());
        int code = -1;
        String sqlQuery = "";
        String motivo;

        
        if (!erroCausaComunicacao(e) && e.getCause() instanceof SQLException) {

            if (e != null) {
                code = ((SQLException)e).getErrorCode();
            }

            
            if (e.getCause() != null) {
                motivo = e.getCause().getMessage();
            } else {
                motivo = e.getMessage();
            }
        } else {
            motivo = e.getMessage();
        }

//        erro.addVariavel(ChavesMonitoradasPadrao.MOTIVO_ERRO.get(), motivo)
//            .addVariavel(ChavesMonitoradasPadrao.ORIGEM_ERRO.get(), getSourceFromStackTraceSqlTrace())
//            .addVariavel(ChavesMonitoradasSQL.SQL_CODE.get(), String.valueOf(code))
//            .addVariavel(ChavesMonitoradasSQL.QUERY_SQL.get(),sqlQuery);
    }

    private String getSourceFromStackTraceSqlTrace() {
        int index = 0;
        if (this.getStackTrace().length > 1) {
            index = 1;
        }
        StackTraceElement stackTrace = this.getStackTrace()[index];
        return String.format("%s - linha: %s." ,stackTrace.getClassName(), stackTrace.getLineNumber());
    }
    
    private boolean erroCausaComunicacao(Throwable cause) throws ErroNegocialException {
		Class<?>[] erros = new Class<?>[2];
		erros[0] = UnknownHostException.class;
		erros[1] = ConnectException.class;
		
		Throwable root = findRoot(cause);
		for (Class<?> object : erros) {
			if (object.isInstance(root)) {
				throw new ErroNegocialException(br.com.caipora.eventos.v1.exceptions.ErrosSistema.ERRO_SISTEMA_INDISPONIVEL.get(), cause);
			}
		}

		return false;
	}
	
	public Throwable findRoot(Throwable throwable) {
	    Objects.requireNonNull(throwable);
	    Throwable rootCause = throwable;
	    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
	        rootCause = rootCause.getCause();
	    }
	    return rootCause;
	}
}