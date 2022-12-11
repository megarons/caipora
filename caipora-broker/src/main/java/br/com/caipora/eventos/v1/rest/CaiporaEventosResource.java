package br.com.caipora.eventos.v1.rest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import br.com.caipora.eventos.v1.models.EventoComunicacao;
import br.com.caipora.eventos.v1.services.EventosService;

@Path("/caipora/v1")
public class CaiporaEventosResource {

    @Inject
    EventosService caiporaEventosService;

    private Logger logger = LoggerFactory.getLogger(CaiporaEventosResource.class);

    /**
     * @throws ErroNegocialException
     * Incluir evento em um topico.
     * 
     * @param subscritor
     * @return
     * @throws ErroNegocialException
     * @throws
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Counted(name = "qtd_incluir_evento", description = "Quantos pedidos de evento foram incluidos no topico.")
    @Timed(name = "tempo_incluir_evento", description = "Tempo para inserir um evento.")
    @Path("/evento")
    public Response incluirEvento(Evento evento) throws ErroNegocialException {
        Instant start = Instant.now();
        try {
            MDC.put("id", "topico=" + evento.getTopico());
            EventoComunicacao ec = caiporaEventosService.incluirEvento(
                    evento);

            return Response.ok(ec).build();

        } finally {
            Instant end = Instant.now();
            logger.debug(">>> END delta=" + Duration.between(start, end).toMillis() + "ms");
            MDC.clear();
        }
    }

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
    public Response buscarProximo(ConfiguracaoConsumidorSubscrito subscritor) throws ErroNegocialException {
        MDC.put("id", UUID.randomUUID().toString() + ",grupo=" + subscritor.getIdGrupo() + ",id="
                + subscritor.getIdExecutor());
        Instant start = Instant.now();
        logger.debug(">>> START <<< ");
        try {
            EventoComunicacao eventoGrupo = caiporaEventosService.buscarProximo(
                    // identificacao do consumidor
                    subscritor.getIdGrupo(),
                    subscritor.getIdExecutor());

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
    @Path("/evento")
    public Response finalizarEvento(EventoComunicacao eventoGrupo) throws ErroNegocialException {
        Instant start = Instant.now();
        try {

            MDC.put("id",
                    UUID.randomUUID().toString() + ",topico=" + eventoGrupo.getEvento().getTopico() + ",offset="
                            + eventoGrupo.getOffsetEvento()
                            + ",particao=" + eventoGrupo.getParticaoSubscricao());
            caiporaEventosService.finalizarEvento(eventoGrupo);

            return Response.ok().build();

        } finally {
            Instant end = Instant.now();
            logger.debug(">>> END delta=" + Duration.between(start, end).toMillis() + "ms");
            MDC.clear();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/broker-status")
    public Response brokerStatus(@QueryParam(value = "topico") String topico) {

        return Response.ok(caiporaEventosService.brokerStatus(topico)).build();
    }

}