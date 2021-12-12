package br.com.caipora.eventos.v1.rest.eventos;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caipora.eventos.v1.integration.BrokerCaiporaResource;
import br.com.caipora.eventos.v1.models.ConfiguracaoConsumidor;
import br.com.caipora.eventos.v1.models.Evento;
import br.com.caipora.eventos.v1.models.PayloadEventoCaipora;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

public abstract class ConsumidorBase implements Runnable {

	@Inject
	@RestClient
	BrokerCaiporaResource brokerCaiporaResource;

	private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

	private Logger logger = LoggerFactory.getLogger(ConsumidorBase.class);

	private String idConsumidor = UUID.randomUUID().toString();

	void onStart(@Observes StartupEvent ev) throws InterruptedException, ExecutionException {
		MDC.put("idConsumidor", idConsumidor);
		logger.info(">>> Inicializando consumidor idConsumidor=" + idConsumidor);
		scheduler.submit(this);

	}

	void onStop(@Observes ShutdownEvent ev) {
		logger.info("<<< Finalizando consumidor... idConsumidor=" + idConsumidor);
		scheduler.shutdown();
		MDC.clear();
	}

	@Override
	public void run() {

		while (true) {
			Instant start = Instant.now();
			logger.trace("[START] >>>" );
			try {
				
				ConfiguracaoConsumidor configuracaoConsumidor = configuracaoConsumidor(idConsumidor);
				try {
					logger.trace("[chamar operacao] >>>" );
					PayloadEventoCaipora proximo = brokerCaiporaResource.buscarProximo(configuracaoConsumidor);
					logger.trace("[resposta operacao] >>>" +proximo.toString());
					if (proximo.getCodigoRetorno()  == 200) {
						logger.trace("[tem mensagem para processar] >>>");
						Instant startCiclo = Instant.now();
						try {
							logger.info("\nsetup    >>>\n\t" + configuracaoConsumidor(idConsumidor));
							logger.info("\nresposta >>>\n\t" + proximo.toString());

							Instant startTratamento = Instant.now();
							try {
								logger.trace("[iniciar tratamento]");
								tratarMensagem(proximo.getEvento());
								logger.trace("[fim tratamento]");
							} finally {
								Instant endTratamento = Instant.now();
								logger.info(">>> END deltaTratamento="
										+ Duration.between(startTratamento, endTratamento).toMillis() + "ms");
							}
							
							

							logger.trace("[iniciar finalizar]");
							brokerCaiporaResource.finalizarEvento(proximo);
							logger.trace("[finalizar finalizar]");
						} finally {
							Instant endCiclo = Instant.now();
							logger.info(
									">>> END deltaCiclo=" + Duration.between(startCiclo, endCiclo).toMillis() + "ms");
						}
					} else {
						logger.debug("DIFERENTE DE 200"+proximo.toString());
						Thread.sleep(500);
					}
				} catch (Exception e) {
					logger.error(e.getMessage());

					try {
						Thread.sleep(10000);
					} catch (InterruptedException e3) {
						logger.error(e.getMessage(), e3);
					}
				}

			} finally {
				Instant end = Instant.now();
				logger.trace(">>> END deltaTotal=" + Duration.between(start, end).toMillis() + "ms");
			}
			
			logger.trace("[END] <<<" );
			
		}
	}

	protected abstract void tratarMensagem(Evento evento);

	protected abstract ConfiguracaoConsumidor configuracaoConsumidor(String idConsumidor);

}
