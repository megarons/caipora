package br.com.caipora.eventos.v1.rest;

import java.time.Duration;
import java.time.Instant;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import br.com.caipora.eventos.v1.exceptions.ErroNegocialException;
import br.com.caipora.eventos.v1.models.ConfiguracaoConsumidorSubscrito;
import br.com.caipora.eventos.v1.models.Evento;
import br.com.caipora.eventos.v1.models.PayloadEventoCaipora;
import br.com.caipora.eventos.v1.services.CaiporaEventosService;

@Path("/caipora/v1")
public class CaiporaEventosResource {

	@Inject
	CaiporaEventosService caiporaEventosService;

	private Logger logger = LoggerFactory.getLogger(CaiporaEventosResource.class);

	/**
	 * @throws ErroNegocialException 
	 * Evento utilizado por um consumidor
	 * 
	 * @param subscritor
	 * @return
	 * @throws ErroNegocialException 
	 * @throws  
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Counted(name = "qtd_buscar_proximo", description = "Quantos pedidos de evento para processamento.")
	@Timed(name = "tempo_buscar_proximo", description = "Tempo para entregar um evento.")
	@Path("/proximo")
	public Response buscarProximo(ConfiguracaoConsumidorSubscrito subscritor) throws  ErroNegocialException {
		Instant start = Instant.now();
		try {
			MDC.put("id", "grupo=" + subscritor.getIdGrupo() + ",id=" + subscritor.getIdExecutor());
			PayloadEventoCaipora eventoGrupo = caiporaEventosService.buscarProximo(
					//identificacao
					subscritor.getIdGrupo(),
					subscritor.getIdExecutor(), 
					// configuracao
					subscritor.getTempoKeepAliveBalanceamentoSegundos(),
					//filtro
					subscritor.getIdentificadorEntrega(),
					subscritor.getEstadoDocumento(), subscritor.getTipologiaDocumento());
			
			
			
			
			eventoGrupo.toString();
			return Response.ok(eventoGrupo).build();


		} finally {
			Instant end = Instant.now();
			logger.debug(">>> END delta=" + Duration.between(start, end).toMillis() + "ms");
			MDC.clear();
		}
	}

	

	/**
	 * Finaliza logicamente um evento de um consumidor na sua tabela de eventos do
	 * grupo.
	 * 
	 * @param eventoGrupo
	 * @return
	 * @throws ErroNegocialException 
	 */
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/")
	public Response finalizarEvento(PayloadEventoCaipora eventoGrupo) throws ErroNegocialException {
		Instant start = Instant.now();
		try {

			MDC.put("id", eventoGrupo.getIdGrupo() + ",offset=" + eventoGrupo.getId_evento_offset()
					+ ",particao=" + eventoGrupo.getParticao() );
			caiporaEventosService.finalizarEvento(eventoGrupo);

			return Response.ok().build();

		} finally {
			Instant end = Instant.now();
			logger.debug(">>> END delta=" + Duration.between(start, end).toMillis() + "ms");
			MDC.clear();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/incluir")
	public Response inserir(Evento ev) throws ErroNegocialException {
			caiporaEventosService.inserirEvento(ev.getProtocolo(), ev.getParticao(), ev.getEntrega(),
					ev.getEstado(), ev.getTipologia());
		logger.info("Evento criado");
		return Response.status(Response.Status.CREATED).build();
	}

	/**
	 * ERRO EH UMA CONDICAO DO CONSUMIDOR. ELE QUEM DEVE TRATAR SEUS ERROS.
	 */
}