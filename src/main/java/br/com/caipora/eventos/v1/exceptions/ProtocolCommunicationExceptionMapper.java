package br.com.caipora.eventos.v1.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.metrics.annotation.Counted;

import br.com.caipora.eventos.v1.models.EventoGrupoCaiporaBrokerComunication;

@Provider
@Counted(name = "ProtocolCommunicationException_Exceptions", absolute = true)
public class ProtocolCommunicationExceptionMapper implements ExceptionMapper<ProtocolCommunicationException> {

	@Override
	public Response toResponse(ProtocolCommunicationException e) {
		EventoGrupoCaiporaBrokerComunication eventoGrupoIMABrokerComunication = new EventoGrupoCaiporaBrokerComunication(Integer.parseInt(e.getCaiporaErro().getCodigo()), e.getCaiporaErro().getMensagem());
		return Response.status(200).entity(eventoGrupoIMABrokerComunication).build();
	}
}