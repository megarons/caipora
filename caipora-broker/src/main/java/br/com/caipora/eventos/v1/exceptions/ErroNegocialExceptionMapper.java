package br.com.caipora.eventos.v1.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ErroNegocialExceptionMapper implements ExceptionMapper<ErroNegocialException> {

		@Override
		public Response toResponse(ErroNegocialException e) {
			ComunicaConsumidorHeader eventoGrupoBrokerComunication = new ComunicaConsumidorHeader(Integer.parseInt(e.getCaiporaErro().getCodigo()), e.getCaiporaErro().getMensagem());
			return Response.status(422).entity(eventoGrupoBrokerComunication).build();
		}
}
