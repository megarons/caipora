package br.com.caipora.eventos.v1.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.metrics.annotation.Counted;

@Provider
@Counted(name = "ComunicaConsumidorWarningMapper", absolute = true)
public class ComunicaConsumidorWarningMapper implements ExceptionMapper<ComunicaConsumidorWarning> {

	@Override
	public Response toResponse(ComunicaConsumidorWarning e) {
		ComunicaConsumidorWarningHeader mensagemConsumidorControle = new ComunicaConsumidorWarningHeader(Integer.parseInt(e.getCaiporaErro().getCodigo()), e.getCaiporaErro().getMensagem());

		//lanca mensagem de header no canal para se comunicar com o consumidor.
		//Ex. nao tem evento para tratar, estrutura esta se rebalanceando,etc...
		return Response.status(200).entity(mensagemConsumidorControle).build();
	}
}