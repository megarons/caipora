package br.com.caipora.eventos.v1.integration;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;


public class BrokerCaiporaExceptionMapper implements ResponseExceptionMapper<BrokerCaiporaException> {

	@Override
	public boolean handles(int statusCode, MultivaluedMap<String, Object> headers) {
		return statusCode != 200;
	}

	@Override
	@Counted(name = "BrokerCaiporaException", absolute = true)
	public BrokerCaiporaException toThrowable(Response response) {
		return new BrokerCaiporaException(response.readEntity(String.class));
	}
}
