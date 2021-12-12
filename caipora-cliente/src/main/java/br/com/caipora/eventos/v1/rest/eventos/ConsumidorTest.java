package br.com.caipora.eventos.v1.rest.eventos;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caipora.eventos.v1.models.ConfiguracaoConsumidor;
import br.com.caipora.eventos.v1.models.Evento;


@ApplicationScoped
public class ConsumidorTest extends ConsumidorBase {
	
	private Logger logger = LoggerFactory.getLogger(ConsumidorTest.class);
	
	@Override
	protected ConfiguracaoConsumidor configuracaoConsumidor(String idConsumidor) {
		
		ConfiguracaoConsumidor consumidor = 
				new ConfiguracaoConsumidor();
		consumidor.setIdGrupo(1);
		consumidor.setIdExecutor(idConsumidor);
		consumidor.setTempoKeepAliveBalanceamentoSegundos(5);
		
		return consumidor;
	}


	@Override
	protected void tratarMensagem(Evento evento) {
		logger.warn(evento.toString());
		
	}





}
