package br.com.caipora.eventos.v1;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException>  {

    private Logger logger = LoggerFactory.getLogger(RuntimeExceptionMapper.class);
	
	@Override
	public Response toResponse(RuntimeException e) {
	    logger.error("Runtime Exception",e);
		return Response.status(500).entity(e.getMessage()).build();
	}

   
}
