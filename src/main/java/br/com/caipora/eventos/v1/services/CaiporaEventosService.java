package br.com.caipora.eventos.v1.services;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caipora.eventos.v1.dao.CaiporaEventosDao;
import br.com.caipora.eventos.v1.exceptions.ErroNegocialException;
import br.com.caipora.eventos.v1.exceptions.ErrosSistema;
import br.com.caipora.eventos.v1.exceptions.ProtocolCommunicationException;
import br.com.caipora.eventos.v1.models.EventoGrupoCaipora;
import br.com.caipora.eventos.v1.models.Particao;
import br.com.caipora.eventos.v1.models.Subscricao;

@RequestScoped
public class CaiporaEventosService {

	@Inject
	CaiporaEventosDao caiporaEventosDao;

	private Logger logger = LoggerFactory.getLogger(CaiporaEventosService.class);

	public int inserirEvento(Long protocolo, int particao, int entrega, int estado, int tipologia)
			throws ErroNegocialException {
		int eventoCriado = caiporaEventosDao.inserirEvento(protocolo, particao, entrega, estado, tipologia);
		return eventoCriado;
	}

	public EventoGrupoCaipora buscarProximo(int idGrupo, String idExecutor, int tempoKeepAlive, int entrega,
			int estado, int tipologia) throws ErroNegocialException {

		EventoGrupoCaipora evento = null;
		// resolver particionamento e rebalanceamento

		int QTD_MAXIMA_PARTICOES_GRUPO = 40; // calcular e colocar no cache 
		int QTD_REGISTROS_POR_PAGINA = 10; // calcular por 3 vezes a quantidade de inscritos

		caiporaEventosDao.matarConfiguracoesClientesInativas(idGrupo, idExecutor, tempoKeepAlive,
				QTD_MAXIMA_PARTICOES_GRUPO);
		Subscricao subscricao = caiporaEventosDao.encontrarSubscricao(idGrupo, idExecutor, QTD_MAXIMA_PARTICOES_GRUPO);
		caiporaEventosDao.atualizarUltimaAtividade(subscricao);
		if (caiporaEventosDao.particoesBalanceadas_verificacaoCentralizadaNoDB(idGrupo)) {

			// buscar mensagens para processar.
			evento = retornarListaTrabalho(idGrupo, idExecutor, subscricao.getParticao(), QTD_REGISTROS_POR_PAGINA,
					entrega, estado, tipologia);
		}

		if (evento == null) {
			throw new ProtocolCommunicationException(ErrosSistema.CC_NAO_FORAM_ENCONTRADOS_REGISTROS.get());
		}
		return evento;

	}

	private EventoGrupoCaipora retornarListaTrabalho(int idGrupo, String idExecutor, List<Particao> particoes,
			int registroPorPagina, int entrega, int estado, int tipologia) throws ErroNegocialException {
		EventoGrupoCaipora proximo = null;

		proximo = caiporaEventosDao.buscarProximo(idGrupo, idExecutor, particoes, registroPorPagina, entrega, estado,
				tipologia);
		if (proximo == null) {
			// tabela de eventos nao tem eventos novos.
			// Sera que eh interessante dormir para o consumidor aqui no provedor ?
			// Thread.sleep(5000);

		}

		return proximo;
	}

	public void finalizarEvento(EventoGrupoCaipora eventoGrupo) throws ErroNegocialException {
		logger.debug("IN");
		caiporaEventosDao.finalizarEvento(eventoGrupo);
		logger.debug("OUT");

	}

}
