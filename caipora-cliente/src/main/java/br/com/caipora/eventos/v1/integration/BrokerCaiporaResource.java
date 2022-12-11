package br.com.caipora.eventos.v1.integration;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import br.com.caipora.eventos.v1.models.ConfiguracaoConsumidor;
import br.com.caipora.eventos.v1.models.EventoComunicacao;

@Path("/caipora/v1")
@Consumes("application/json; charset=UTF-8")
@RegisterRestClient(configKey="caipora-api")
@RegisterProvider(BrokerCaiporaExceptionMapper.class)
public interface BrokerCaiporaResource {
	
	@POST
	@Path("/proximo")
	//Resetar offset sera uma opcao feita pelo administrador em interface especifica
	EventoComunicacao buscarProximo(ConfiguracaoConsumidor r) throws BrokerCaiporaException;

	@DELETE
	@Path("/evento")
	void finalizarEvento(EventoComunicacao e) throws BrokerCaiporaException;

}
