package br.com.caipora.eventos.v1.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caipora.eventos.v1.dao.BalanceamentoConsumidoresModDAO;
import br.com.caipora.eventos.v1.dao.EventosDAO;
import br.com.caipora.eventos.v1.exceptions.ComunicaConsumidorWarning;
import br.com.caipora.eventos.v1.exceptions.ErroNegocialException;
import br.com.caipora.eventos.v1.exceptions.ErrosSistema;
import br.com.caipora.eventos.v1.models.BrokerStatus;
import br.com.caipora.eventos.v1.models.Evento;
import br.com.caipora.eventos.v1.models.EventoComunicacao;
import br.com.caipora.eventos.v1.models.Subscricao;



@ApplicationScoped
public class EventosService {

	@Inject
	EventosDAO eventoDao;
	
    @Inject
    BalanceamentoConsumidoresModDAO balanceamentoConsumidoresDao;

	private Logger logger = LoggerFactory.getLogger(EventosService.class);


    public EventoComunicacao incluirEvento(Evento evento) throws ErroNegocialException {
        return eventoDao.inserirEvento(evento);
    }
	
	public EventoComunicacao buscarProximo(String textoBindTopico, String idConsumidor) throws ErroNegocialException {

		EventoComunicacao evento = null;
		
		
		int _CONSUMIDOR_TEMPO_KEEP_ALIVE = 5;
		
		
		/**
		 * TODO Rever como melhoria futura.
		 * Apos simplificar retirando as particoes das tabelas de eventos penso que a estrutura atual de balanceamento pode ser muitissimo simplificada
		 * pois eh possivel pensar que quem esta ativo durante um tempo esta estavel e balanceado. A particao de balanceamento pode ser baseado na 
		 * sequencia de registros dos subscritos ativos sendo as particoes virtuais iguais ao indice do array da quantidade de registros, o mod pode 
		 * ser o size desse mesmo array e o lider pode ser sempre o primeiro indice vist que quando houver rebalanceamento tudo iniciara do zero 
		 * novamente.
		 * 
		 */
		Subscricao subscricao = balanceamentoConsumidoresDao.atualizaProvaDeVidaConsumidor(textoBindTopico, idConsumidor, _CONSUMIDOR_TEMPO_KEEP_ALIVE);
		if (balanceamentoConsumidoresDao.consumidoresGrupoEstaoBalanceados_verificacaoCentralizadaNoDB(textoBindTopico, idConsumidor,_CONSUMIDOR_TEMPO_KEEP_ALIVE)) {
			// buscar evento para processar.
			evento = eventoDao.buscarProximo(textoBindTopico, idConsumidor, ((subscricao.getParticao().size()==0)?0:subscricao.getParticao().get(0).getParticao()),(long)balanceamentoConsumidoresDao.qtdConsumidoresTopico(textoBindTopico));
		}

		if (evento == null) {
			throw new ComunicaConsumidorWarning(ErrosSistema.CC_NAO_FORAM_ENCONTRADOS_REGISTROS.get());
		}
		evento.setHashIntegridade(geraHash(evento));
		logger.info("\nBUSCOU_PROXIMO|offset="+evento.getOffsetEvento()+",\npayload="+evento.getEvento().getPayload() +",\ndistribuidoPara={idGrupo="+textoBindTopico+",idConsumidor="+idConsumidor+", particao="+subscricao.getParticao()+" }");
		return evento;

	}
	
	



	public void finalizarEvento(EventoComunicacao evento) throws ErroNegocialException {
		//TODO adicionar idConsumidor no payload pra logar no tx qual foi o consumidor que consumiu o evento
		
		logger.warn("evento=FINALIZAR_EVENTO,offset="+evento.getOffsetEvento()+"payload="+evento.toString());
		String hashEvento = geraHash(evento);
		if(hashEvento.equals(evento.getHashIntegridade())){
			eventoDao.finalizarEvento(evento);
			logger.warn("evento=EVENTO_FINALIZADO,offset="+evento.getOffsetEvento());
		}else {
			throw new ErroNegocialException(ErrosSistema.ERRO_NEGOCIAL_HASH_INTEGRIDADE_NAO_CONFERE.get());
		}

	}
	
	
	private String geraHash(EventoComunicacao evento) throws ErroNegocialException {
		
		return geraHash(evento.getEvento().getTopico(), evento.getOffsetEvento(), evento.getEstadoProcessamento(), evento.getRecorrencia(), 
				evento.getTimestampInclusaoRegistro(), evento.getTimestampUltimaAtualizacao());
	}
	/**
	 * Gera hash do Payload de evento ocorrido. A ideia dessa estrutura é poder
	 * validar o evento como integro quando esse interage entre duas estruturas ou
	 * momentos de processamento distintos.
	 * 
	 * @param headerConfiguracaoEvento
	 * @return
	 * @throws ErroNegocialException
	 */
	private String geraHash(String grupo, long id_evento_offset, String estado_processamento, int recorrencia,
			Timestamp timestampInclusaoRegistro, Timestamp timestampUltimaAtualizacao) throws ErroNegocialException {

		MessageDigest md;
		try {
			String text =  grupo+""+id_evento_offset+""+estado_processamento+""+recorrencia+""+
		((timestampInclusaoRegistro == null)?"": timestampInclusaoRegistro.toString())+""+
		((timestampUltimaAtualizacao == null)?"": timestampUltimaAtualizacao.toString());
					
			md = MessageDigest.getInstance("MD5");
			md.update(text.getBytes());
			byte[] digest = md.digest();
			String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
			return myHash;
		} catch (NoSuchAlgorithmException e) {
			throw new ErroNegocialException(ErrosSistema.ERRO_NEGOCIAL_GERA_HASH.get());

		}
	}

    public List<BrokerStatus> brokerStatus(String textoBindTopico) {
        List<BrokerStatus> status = null;
        try {
            status = balanceamentoConsumidoresDao.getStatus(textoBindTopico);
        } catch (ErroNegocialException e) {
            e.printStackTrace();
        }
        return status;
    }




}
