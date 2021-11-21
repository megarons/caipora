package br.com.caipora.eventos.v1.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import br.com.caipora.eventos.v1.models.EventoGrupoCaiporaBrokerComunication;

public class ErroNegocialExceptionMapper implements ExceptionMapper<ErroNegocialException> {

		@Override
		public Response toResponse(ErroNegocialException e) {
			EventoGrupoCaiporaBrokerComunication eventoGrupoIMABrokerComunication = new EventoGrupoCaiporaBrokerComunication(Integer.parseInt(e.getCaiporaErro().getCodigo()), e.getCaiporaErro().getMensagem());
			return Response.status(422).entity(eventoGrupoIMABrokerComunication).build();
		}
}
