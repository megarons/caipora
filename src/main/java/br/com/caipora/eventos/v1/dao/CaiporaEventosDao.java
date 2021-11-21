package br.com.caipora.eventos.v1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caipora.eventos.v1.exceptions.ErroNegocialException;
import br.com.caipora.eventos.v1.exceptions.ErroSqlException;
import br.com.caipora.eventos.v1.exceptions.ErrosSistema;
import br.com.caipora.eventos.v1.exceptions.ProtocolCommunicationException;
import br.com.caipora.eventos.v1.models.EventoGrupoCaipora;
import br.com.caipora.eventos.v1.models.Particao;
import br.com.caipora.eventos.v1.models.Subscricao;
import io.agroal.api.AgroalDataSource;

@ApplicationScoped
public class CaiporaEventosDao {

	private static final int _ESTADO_PROCESSAMENTO_FINALIZADO = 2;

	private static final int _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO = 1;

	private Logger logger = LoggerFactory.getLogger(CaiporaEventosDao.class);

	@Inject
	AgroalDataSource defaultDataSource;

	private Long gerarNovoSequencial() throws  ErroNegocialException {
		logger.trace("CaiporaEventosDao.gerarNovoSequencial()");
		String sqlAtualizaGrupo = new StringBuilder()
				.append("SELECT CONCAT(")
				.append("EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::TEXT,")
				.append("EXTRACT(DOY FROM CURRENT_TIMESTAMP)::TEXT,")
				.append("LPAD(nextval('eventos.evento_sequence')::TEXT, 10, '0'));")
				.toString();

		Long sequencial = null;
		try(Connection con = defaultDataSource.getConnection();
		PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);
		ResultSet rs = prepareStatement.executeQuery();){
			if (rs.next()) {
				sequencial = rs.getLong(1);
				logger.info("Sequencial: " + sequencial);
			}
			if (sequencial == null) {
				throw new IllegalStateException("Valor do sequencial é nulo");
			}
		} catch (SQLException e) {
			logger.error("Erro em gerarNovoSequencial() inativos", e);
			throw new ErroSqlException(e);
		}
		return sequencial;
	}

	public int inserirEvento(Long protocolo, int particao, int entrega, int estado, int tipologia)
			throws  IllegalStateException, ErroNegocialException {
		logger.trace("CaiporaEventosDao.inserirEvento()");

		Long sequencial = this.gerarNovoSequencial();
		int rowsCreated = 0;

		String sqlInserirEvento = new StringBuilder().append("INSERT INTO \"eventos\".eventos_mensagens ")
				.append("(id_evento_offset, particao, ts_inclusao_registro, ptl, entrega, estado, tipologia) ")
				.append("VALUES(?, ?, ?, ?, ?, ?, ?);").toString();

		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlInserirEvento);) {
			prepareStatement.setLong(1, sequencial);
			prepareStatement.setInt(2, particao);
			prepareStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			prepareStatement.setLong(4, protocolo);
			prepareStatement.setInt(5, entrega);
			prepareStatement.setInt(6, estado);
			prepareStatement.setInt(7, tipologia);
			rowsCreated = prepareStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("Erro em inserirEvento() ", e);
			throw new ErroSqlException(e);
		}
		if (rowsCreated == 0) {
			throw new ErroNegocialException(ErrosSistema.ERRO_AO_INSERIR_EVENTO.get());
		}
		return rowsCreated;
	}

	public void atualizarUltimaAtividade(Subscricao subscricao) throws ErroNegocialException  {
		logger.trace("CaiporaEventosDao.atualizarUltimaAtividade()");
		String sqlAtualizaGrupo = "UPDATE \"eventos\".subscricoes SET ts_ultima_atividade = now() WHERE cd_grupo = ? and id_executor = ? ";
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);) {
			prepareStatement.setInt(1, subscricao.getCd_grupo());
			prepareStatement.setString(2, subscricao.getId_executor());

			prepareStatement.execute();

		} catch (SQLException e) {
			logger.error("Erro em atualizarUltimaAtividade",e);
			throw new ErroSqlException(e);
		}

	}

	public int verificarQuantidadeDocumentosEnfileirados(String cd_grupo, String id_executor) throws SQLException {
		String sqlDocumentosEnfileirados = "with particoes as (" + "	select" + "		a.particao" + " from"
				+ "		\"eventos\".subscricoes_particoes a" + " where" + "		a.cd_grupo = ? and a.id_executor = ?"
				+ "	)"
				+ " select count(-1) from \"eventos\".eventos_grupo a, particoes b where a.cd_grupo = ? and a.particao in (b.particao)  ";

		int backPressure = 0;
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlDocumentosEnfileirados);) {
			prepareStatement.setString(1, cd_grupo);
			prepareStatement.setString(2, id_executor);
			prepareStatement.setString(3, cd_grupo);

			try (ResultSet rs = prepareStatement.executeQuery();) {
				while (rs.next()) {
					backPressure = rs.getInt(1);

				}
			}
			logger.debug("BACKPRESSURE=" + backPressure);
		}
		return backPressure;
	}

	private String particaoToSQL(List<Particao> particao) {
		StringBuilder str = new StringBuilder();
		for (Particao registro : particao) {
			str.append(registro.getParticao()).append(", ");
		}

		String particoesSQL = (!str.toString().equals("")) ? str.toString().substring(0, str.toString().length() - 2)
				: "";
		return particoesSQL;
	}

	public Subscricao encontrarSubscricao(int idGrupo, String idExecutor, int qtdMaximaParticoes) throws  ErroNegocialException {
		logger.trace("CaiporaEventosDao.encontrarSubscricao()");
		String sqlEncontrarSubscricao = "SELECT cd_grupo, id_executor, ts_ultima_atividade FROM \"eventos\".subscricoes WHERE cd_grupo = ? and id_executor = ? ";
		Subscricao subscricao = null;
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarSubscricao);) {
			prepareStatement.setInt(1, idGrupo);
			prepareStatement.setString(2, idExecutor);

			try (ResultSet rs = prepareStatement.executeQuery();) {
				while (rs.next()) {
					subscricao = new Subscricao(rs.getInt(1), rs.getString(2), getParticoes(idGrupo, idExecutor), rs.getTimestamp(3));
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
			throw new ErroSqlException(e);
		}

		if (subscricao == null) {

			if (getSubscricoes(idGrupo).size() < qtdMaximaParticoes) {
				// tem que cadastrar
				return cadastrarMembro(idGrupo, idExecutor, qtdMaximaParticoes);
			} else {
				throw new ErroNegocialException(br.com.caipora.eventos.v1.exceptions.ErrosSistema.ATINGIDO_LIMITE_MAXIMO_DE_CONSUMIDORES_CONCORRENTES.get());
			}

		}
		return subscricao;

	}

	private List<Particao> getParticoes(int idGrupo, String idExecutor) throws SQLException {
		logger.trace("CaiporaEventosDao.getParticoes()");
		String sqlEncontrarSubscricao = "SELECT particao FROM \"eventos\".subscricoes_particoes WHERE cd_grupo = ? and id_executor = ? ";
		List<Particao> lista = null;
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarSubscricao);) {
			prepareStatement.setInt(1, idGrupo);
			prepareStatement.setString(2, idExecutor);

			try (ResultSet rs = prepareStatement.executeQuery();) {
				lista = new ArrayList<>();
				while (rs.next()) {
					lista.add(new Particao(idGrupo, idExecutor, rs.getInt(1)));
				}
			}
		}
		logger.info("Lista #### size="+lista.size());
		return lista;
	}

	public void matarConfiguracoesClientesInativas(int idGrupo, String idExecutor, int tempoLimiteBalanceamento, int qtdMaximaParticoes) throws ErroNegocialException {
		logger.trace("CaiporaEventosDao.matarConfiguracoesClientesInativas()");
		
		//TODO avaliar se vale dar lock pelo lider aqui
		
		int tempoLimiteInatividadeSegundos = tempoLimiteBalanceamento;
		String sqlDeletarSubscricao = "DELETE FROM \"eventos\".subscricoes WHERE ts_ultima_atividade < now() - interval '"
				+ tempoLimiteInatividadeSegundos + " seconds' ";
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlDeletarSubscricao);) {
			prepareStatement.execute();
		} catch (SQLException e) {
			logger.error("Erro em matarConfiguracoesClientesInativas() inativos", e);
			throw new ErroSqlException(e);
		}
		
		logger.trace("CaiporaEventosDao.matarConfiguracoesClientesInativas()");
		
		String sqlDeletarSubscricaoParticoes = "DELETE FROM \"eventos\".subscricoes_particoes WHERE id_executor NOT IN (select id_executor FROM \"eventos\".subscricoes)";
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlDeletarSubscricaoParticoes);) {
			prepareStatement.execute();
		} catch (SQLException e) {
			logger.error("Erro em matarConfiguracoesClientesInativas() subscricoes", e);
			throw new ErroSqlException(e);
		}
		
		
		rebalancearParticoes(idGrupo,idExecutor, qtdMaximaParticoes);
	}

	private void limparBalanceamento(int grupo) throws ErroNegocialException  {
		logger.trace("CaiporaEventosDao.limparBalanceamento()");
		String sqlDeletarParticoes = "DELETE FROM \"eventos\".subscricoes_particoes where cd_grupo = ?";
		logger.debug("CaiporaEventosDao.limparBalanceamento() " + sqlDeletarParticoes);
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlDeletarParticoes);) {
			prepareStatement.setInt(1, grupo);
			prepareStatement.execute();
		} catch (SQLException e) {
			logger.error("Erro em limparBalanceamento()",e);
			throw new ErroSqlException(e);
		}
	}

	private Subscricao cadastrarMembro(int idGrupo, String idExecutor, int qtdMaximaParticoes) throws ErroNegocialException {
		logger.trace("CaiporaEventosDao.cadastrarMembro()");
		incluirSubscricao(idGrupo,idExecutor);
		rebalancearParticoes(idGrupo,idExecutor, qtdMaximaParticoes);
		return encontrarSubscricao(idGrupo,idExecutor,qtdMaximaParticoes);
	}

	private void incluirSubscricao(int idGrupo, String idExecutor) throws ErroNegocialException {
		logger.trace("CaiporaEventosDao.incluirSubscricao()");
		String sqlIncluirSubscricao = "INSERT INTO \"eventos\".subscricoes (cd_grupo, id_executor, ts_ultima_atividade) VALUES ( ?, ?, now() )  ";
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlIncluirSubscricao);) {

			prepareStatement.setInt(1, idGrupo);
			prepareStatement.setString(2, idExecutor);
	
			prepareStatement.execute();
		} catch (SQLException e) {
			logger.error("Erro ao incluir consumidor", e);
			throw new ErroSqlException(e);
		}
	}

	private void rebalancearParticoes(int idGrupo, String idExecutor, int qtdMaximaParticoes) throws ErroNegocialException {
		logger.trace("rebalancearParticoes");

		if (!particoesBalanceadas_verificacaoCentralizadaNoDB(idGrupo)) {

			// Elege id do leader
			String leader = getLeader_verificacaoCentralizadaNoDB(idGrupo);
			logger.info("EU SOU O EXECUTANTE DO MOMENTO idGrupo="+idGrupo+",id=" + idExecutor + ", leader=" + leader);
			// travar execucao pelo leader
			if (idExecutor.equals(leader)) {
				logger.info("LIDER EM ACAO idGrupo="+idGrupo+",id=" + idExecutor + ", leader=" + leader);
					limparBalanceamento(idGrupo);
	
					List<Subscricao> subscricoes = getSubscricoes(idGrupo);
	
					logger.info(" ANTES subscricoes=" + subscricoes);
					if (subscricoes.size() > 0) {
	
						Object[][] resposta = new Object[qtdMaximaParticoes][3];
						// recalcular balanceamento
	
						int ct = 0;
						int limit = subscricoes.size();
						for (int j = 0; j < qtdMaximaParticoes; j++) {
							Subscricao subscricaoObjeto = (Subscricao) subscricoes.get(ct);
							resposta[j][0] = subscricaoObjeto.getCd_grupo();
							resposta[j][1] = subscricaoObjeto.getId_executor();
							resposta[j][2] = j;
							ct++;
	
							if (ct == limit) {
								ct = 0;
							}
							
							
						}
	
						logger.info(" DEPOIS subscricoes=" + subscricoes);
	
						String sqlIncluirParticoes = "INSERT INTO \"eventos\".subscricoes_particoes ( cd_grupo, id_executor, particao) VALUES (?, ?, ?)  ";
						try (Connection con = defaultDataSource.getConnection();
								PreparedStatement prepareStatement = con.prepareStatement(sqlIncluirParticoes);) {
							con.setAutoCommit(false);
							for (int j = 0; j < qtdMaximaParticoes; j++) {
								
								prepareStatement.setInt(1, (int) resposta[j][0]);
								prepareStatement.setString(2, (String) resposta[j][1]);
								prepareStatement.setInt(3, ((int) resposta[j][2] + 1));
								prepareStatement.addBatch();
	
							}
							prepareStatement.executeBatch();
							con.commit();
						
						} catch (SQLException e) {
							logger.error("Erro ao rebalancearParticoes", e);
							throw new ErroSqlException(e);
						}
					}
			} 
			
//			else {
//				logger.debug(" - Sem subscricoes para balancear nao sou o leader - leader=" + leader + ", cp="
//						+ idExecutor);
//			}
			
			throw new ProtocolCommunicationException(br.com.caipora.eventos.v1.exceptions.ErrosSistema.CC_REBALANCEANDO_CONSUMIDORES.get());
			
		} else {

			logger.debug(" ESTA BALANCEADO !!!");

		}

	}

	public boolean particoesBalanceadas_verificacaoCentralizadaNoDB(int cd_grupo) throws ErroNegocialException  {
		String sqlParticoesBalaceadas = "with " + " subcricao_executores as ("
				+ "		select a.id_executor from 	\"eventos\".subscricoes_particoes a where  a.cd_grupo = ? group by id_executor  ),	"
				+ " subscricoes_ativas as ( "
				+ "		select 	a.id_executor  from 	\"eventos\".subscricoes a where a.cd_grupo = ? ), 	 "
				+ " diferentes as ( "
				+ "		select * from subscricoes_ativas a where not exists ( select 1 from subcricao_executores b where a.id_executor = b.id_executor) "
				+ "		union all "
				+ "		select * from subcricao_executores a where not exists ( select 1 from subscricoes_ativas  b where a.id_executor = b.id_executor) "
				+ ") " + " select count(-1) from diferentes";

		int qtdDesbalanceados = 1;
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlParticoesBalaceadas);) {
			prepareStatement.setInt(1, cd_grupo);
			prepareStatement.setInt(2, cd_grupo);

			try (ResultSet rs = prepareStatement.executeQuery();) {
				while (rs.next()) {
					qtdDesbalanceados = rs.getInt(1);
				}
			}
			if (qtdDesbalanceados != 0) {
				logger.info("ESTA DESBALANCEADO !!!");
			}
		} catch (SQLException e) {
			logger.error("Erro particoesBalanceadas_verificacaoCentralizadaNoDB", e);
			throw new ErroSqlException(e);
		}
		return ((qtdDesbalanceados == 0) ? true : false);

	}

	private String getLeader_verificacaoCentralizadaNoDB(int cd_grupo) throws ErroNegocialException   {

		String sqlEncontrarLeader = "with " + "executores as ("
				+ " 	select id_executor from \"eventos\".subscricoes_particoes where  cd_grupo = ? group by id_executor   "
				+ ")  " + "select \n" + "		a.id_executor from \"eventos\".subscricoes as a, executores as b "
				+ "where \n" + " 	cd_grupo = ? and	a.id_executor = b.id_executor"
				+ "	order by	id_executor asc	limit 1";

		String leader = null;
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarLeader);) {
			prepareStatement.setInt(1, cd_grupo);
			prepareStatement.setInt(2, cd_grupo);

			try (ResultSet rs = prepareStatement.executeQuery();) {
				while (rs.next()) {
					leader = rs.getString(1);

				}
			}
		} catch (SQLException e) {
			logger.error("Erro getLeader_verificacaoCentralizadaNoDB", e);
			throw new ErroSqlException(e);
		}
		if (leader == null) {
			// eleger lider da tabela de subscricao - condicao quando nao iniciou nenhum
			// subscritor ainda.

			//String sqlEncontrarLeaderSubscricao = " select id_executor from \"eventos\".subscricoes order by ts_ultima_atividade asc limit 1 ";
			
			String sqlEncontrarLeaderSubscricao = "  select id_executor from \"eventos\".subscricoes where ts_ultima_atividade < now() limit 1";

			try (Connection con = defaultDataSource.getConnection();
					PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarLeaderSubscricao);
					ResultSet rs = prepareStatement.executeQuery();) {

				while (rs.next()) {
					leader = rs.getString(1);

				}
			} catch (SQLException e) {
				logger.error("Erro getLeader_verificacaoCentralizadaNoDB leader null", e);
				throw new ErroSqlException(e);
			}

		}

		return leader;
	}

	private List<Subscricao> getSubscricoes(int cd_grupo) throws ErroNegocialException  {
		logger.trace("CaiporaEventosDao.getSubscricoes()");
		String sqlEncontrarMembrosGrupo = "SELECT * FROM \"eventos\".subscricoes WHERE  cd_grupo = ? ";
		List<Subscricao> lista = null;
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarMembrosGrupo);) {
			prepareStatement.setInt(1, cd_grupo);

			try (ResultSet rs = prepareStatement.executeQuery();) {
				lista = new ArrayList<Subscricao>();
				while (rs.next()) {
					lista.add(new Subscricao(rs.getInt(1), rs.getString(2), new ArrayList<>(), rs.getTimestamp(3)));

				}
			}
		} catch (SQLException e) {
			logger.error("Erro em getSubscricoes()",e);
			throw new ErroSqlException(e);
		}
		
		return lista;
	}

	public EventoGrupoCaipora buscarProximo(int idGrupo, String idExecutor, List<Particao> particoes, int registroPorPagina,  int entrega, int estado, int tipologia) throws ErroNegocialException
			{

		EventoGrupoCaipora eventoGrupo = buscarProximoTabelaEventoGrupo(idGrupo,idExecutor, particoes);
		if (eventoGrupo == null) {
			int paginacaoConsumidores = registroPorPagina;
			logger.info("paginacaoConsumidores="+paginacaoConsumidores);
			carregarTabelaEventoGrupo(idGrupo,idExecutor,paginacaoConsumidores , entrega,estado,tipologia);
			eventoGrupo = buscarProximoTabelaEventoGrupo(idGrupo,idExecutor, particoes);

		}

		if (eventoGrupo != null) {
			updateRecorrencia(eventoGrupo);
		}

		return eventoGrupo;
	}

	private void updateRecorrencia(EventoGrupoCaipora eventoGrupo) throws ErroNegocialException {
		logger.trace("updateEstadoTratamentoParaPendenteConsumidor");
		String sqlAtualizaGrupo = "UPDATE \"eventos\".eventos_grupo SET recorrencia = recorrencia + 1  WHERE cd_grupo = ? and particao = ? and id_evento_offset = ? and ptl = ? and estado_processamento = ?";

		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);) {
			
			eventoGrupo.setRecorrencia(eventoGrupo.getRecorrencia()+1);// feito aqui para evitar uma consulta sql 
			
			try (PreparedStatement statement = atualizaHistorico(con, eventoGrupo, _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO);) {
				prepareStatement.setInt(1, eventoGrupo.getIdGrupo());
				prepareStatement.setLong(2, eventoGrupo.getParticao());
				prepareStatement.setLong(3, eventoGrupo.getId_evento_offset());
				prepareStatement.setLong(4, eventoGrupo.getPtl());
				prepareStatement.setInt(5, _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO);

				prepareStatement.execute();
			}

		} catch (SQLException e) {
			logger.error("Erro em updateRecorrencia()", e);
			throw new ErroSqlException(e);
		}
	}

	private PreparedStatement atualizaHistorico(Connection con, EventoGrupoCaipora eventoGrupo, int estado) throws SQLException {
		String sqlAtualizaHistoricoGrupo;
			sqlAtualizaHistoricoGrupo = "INSERT INTO \"eventos\".historico_eventos_grupo "
				+ "("
				+ "cd_grupo,id_evento_offset,ts_atualizacao_evento,particao,estado_processamento,recorrencia,ts_inclusao_registro,ts_finalizacao_registro,ptl,entrega,estado,tipologia)"
				+ "VALUES "
				+ "(?,?,now(),?,?,?,?,?,?,?,?,? )";
			
			
			try(PreparedStatement psHist = con.prepareStatement(sqlAtualizaHistoricoGrupo);){
				psHist.setInt(1, eventoGrupo.getIdGrupo());
				psHist.setLong(2, eventoGrupo.getId_evento_offset());
				//3 now
				psHist.setInt(3, eventoGrupo.getParticao());
				psHist.setInt(4, estado);
				psHist.setInt(5, eventoGrupo.getRecorrencia());
				psHist.setTimestamp(6, eventoGrupo.getTimestampInclusaoRegistro());
				psHist.setTimestamp(7,  new Timestamp(System.currentTimeMillis()));// mudar o insert para fazer o select na tabela evento_grupo ao inves de montar o objeto
				psHist.setLong(8, eventoGrupo.getPtl());
				psHist.setInt(9, eventoGrupo.getEntrega());
				psHist.setLong(10, eventoGrupo.getEstado());
				psHist.setLong(11, eventoGrupo.getTipologia());
				
				psHist.execute();
				return psHist;
			}
		
	}


	private void carregarTabelaEventoGrupo(int idGrupo, String idExecutor, int registrosPorPagina, int entrega, int estado,int tipologia) throws ErroNegocialException  {
		logger.trace(" >>>>> carregarTabelaEventoGrupo");

		// Elege id do leader
		String leader = getLeader_verificacaoCentralizadaNoDB(idGrupo);
		logger.info("\nid_executor=" + idExecutor + ", \nid_leader=  " + leader);
		// travar execucao pelo leader
		if (idExecutor.equals(leader)) {
				String sqlReloadEventos = montaSqlReload(idGrupo, _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO,
						"" + entrega, "" + estado,
						"" + tipologia, registrosPorPagina);
	
	
				logger.debug(sqlReloadEventos);
	
				try (Connection con = defaultDataSource.getConnection(); Statement psQuery = con.createStatement();) {
	
					List<EventoGrupoCaipora> eventos = new ArrayList<EventoGrupoCaipora>();
					try (ResultSet rs = psQuery.executeQuery(sqlReloadEventos);) {
						while (rs.next()) {
							
							eventos.add(new EventoGrupoCaipora(rs.getInt(1), rs.getLong(4), rs.getInt(2), rs.getLong(3),
									rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(5), rs.getTimestamp(9)));
						}
	
					}
	
					if (eventos.size() > 0) {
						
						String sqlInsertEventoGrupo = "INSERT INTO eventos.eventos_grupo  \n"
								+ "( cd_grupo, id_evento_offset, particao,  estado_processamento, recorrencia, ts_inclusao_registro, ptl, entrega, estado, tipologia ) \n"
								+ "VALUES ( ?, ?, ?, ?, 0, now() ,? ,? ,? , ? )\n";
						
						String sqlAtualizaHistoricoGrupo = "INSERT INTO \"eventos\".historico_eventos_grupo "
								+ "( cd_grupo, id_evento_offset, ts_atualizacao_evento, particao, estado_processamento, recorrencia, ts_inclusao_registro, ptl, entrega, estado, tipologia)"
								+ "VALUES "
								+ "(?,?,now(),?,?,0, now(),?,?,?,? )";
	
						try (PreparedStatement psInsertEvento = con.prepareStatement(sqlInsertEventoGrupo);) {
						try(PreparedStatement psHist = con.prepareStatement(sqlAtualizaHistoricoGrupo);){
							con.setAutoCommit(false);
	
							for (EventoGrupoCaipora eventoGrupo : eventos) {
								psInsertEvento.setInt(1, eventoGrupo.getIdGrupo());
								psInsertEvento.setLong(2, eventoGrupo.getId_evento_offset());
								psInsertEvento.setLong(3, eventoGrupo.getParticao());
								psInsertEvento.setInt(4, eventoGrupo.getEstado_processamento());
								psInsertEvento.setLong(5, eventoGrupo.getPtl());
								psInsertEvento.setInt(6, eventoGrupo.getEntrega());
								psInsertEvento.setInt(7, eventoGrupo.getEstado());
								psInsertEvento.setInt(8, eventoGrupo.getTipologia());
								psInsertEvento.addBatch();
								
								psHist.setInt(1, eventoGrupo.getIdGrupo());
								psHist.setLong(2, eventoGrupo.getId_evento_offset());
								//3 now
								psHist.setInt(3, eventoGrupo.getParticao());
								psHist.setInt(4, eventoGrupo.getEstado_processamento());

								
								psHist.setLong(5, eventoGrupo.getPtl());
								psHist.setInt(6, eventoGrupo.getEntrega());
								psHist.setLong(7, eventoGrupo.getEstado());
								psHist.setLong(8, eventoGrupo.getTipologia());
								
								psHist.addBatch();
	
							}
							psInsertEvento.executeBatch();
							psHist.executeBatch();
	
							con.commit();
						}
					}
					}
				} catch (SQLException e1) {
					logger.error("Erro ao carregar tabela de eventos na tabela de evento do grupo", e1);
					throw new ErroSqlException(e1);
				}
		}

	}

	private String montaSqlReload(int cd_grupo, int estadoProcessamento, String entrega, String estado,
			String tipologia, int registrosPorPagina) {
		StringBuilder query = new StringBuilder();

		query.append("select\n");
		query.append("'" + cd_grupo + "', origem.particao, origem.id_evento_offset ,origem.ptl,\n");
		query.append("'" + estadoProcessamento+ "', origem.entrega, origem.estado, origem.tipologia, now()\n");
		query.append("from\n");
		query.append("eventos.eventos_mensagens as origem\n");
		query.append("where\n");
		query.append("(\n");
		query.append("origem.id_evento_offset > (\n");
		query.append("select id_evento_offset from eventos.eventos_grupo WHERE cd_grupo = '" + cd_grupo + "' \n");
		query.append("order by id_evento_offset desc limit 1\n");
		query.append(")\n");
		query.append("or not exists (select 1 from eventos.eventos_grupo WHERE cd_grupo = '" + cd_grupo + "' )\n");
		query.append(")\n");
		if (entrega != null && !"".equals(entrega) && !"0".equals(entrega)) {
			query.append("and origem.entrega = " + entrega + "\n");
		}
		if (estado != null && !"".equals(estado) && !"0".equals(estado)) {
			query.append("and origem.estado = " + estado + "\n");
		}
		if (tipologia != null && !"".equals(tipologia) && !"0".equals(tipologia)) {
			query.append("and origem.tipologia = " + tipologia + "\n");
		}
		query.append("order by origem.id_evento_offset asc\n");
		query.append("limit " + registrosPorPagina + "\n");
		return query.toString();
	}

	private EventoGrupoCaipora buscarProximoTabelaEventoGrupo(int idGrupo, String idExecutor, List<Particao> particoes) throws ErroNegocialException
			 {
		String particoesSQL = particaoToSQL(particoes);

		logger.info("particoesSize="+particoes.size()+",particoes="+particoesSQL);
		String sqlDocumentosParaProcessamento = " SELECT "
				+ "	cd_grupo, id_evento_offset, particao, estado_processamento, recorrencia, ts_inclusao_registro,  ptl,entrega, estado, tipologia   "
				+ " FROM eventos.eventos_grupo  WHERE "
				+ "	estado_processamento = "+_ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO+" and cd_grupo = ? and particao in (" + particoesSQL
				+ ") order by id_evento_offset asc limit 1 ";

		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlDocumentosParaProcessamento);) {
			prepareStatement.setInt(1, idGrupo);

			try (ResultSet rs = prepareStatement.executeQuery();) {
				EventoGrupoCaipora evento = null;
				while (rs.next()) {
					evento = new EventoGrupoCaipora(rs.getInt(1), rs.getLong(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getTimestamp(6),
							rs.getLong(7), rs.getInt(8), rs.getInt(9), rs.getInt(10));
					return evento;
				}
				return evento;
			}
		} catch (SQLException e) {
			logger.error("Erro em buscarProximoTabelaEventoGrupo()", e);
			throw new ErroSqlException(e);
		}

	}

	public void finalizarEvento(EventoGrupoCaipora eventoGrupo) throws ErroNegocialException  {
		logger.trace("CaiporaEventosDao.finalizarProcessamentoEvento");
		
		
		
		
		String sqlAtualizaFimProcessamento = "UPDATE eventos.eventos_grupo\n"
				+ "SET estado_processamento=?, ts_finalizacao_registro=now() \n"
				+ "WHERE id_evento_offset=? AND cd_grupo=? AND particao=?;";
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaFimProcessamento);) {

			con.setAutoCommit(false);

			try (PreparedStatement statement = atualizaHistorico(con, eventoGrupo, _ESTADO_PROCESSAMENTO_FINALIZADO);) {
				prepareStatement.setInt(1, _ESTADO_PROCESSAMENTO_FINALIZADO);
				prepareStatement.setLong(2, eventoGrupo.getId_evento_offset());
				prepareStatement.setInt(3, eventoGrupo.getIdGrupo());
				prepareStatement.setLong(4, eventoGrupo.getParticao());

				prepareStatement.execute();
			}
			con.commit();

		} catch (SQLException e) {
			logger.error("Erro em finalizarEvento()", e);
			throw new ErroSqlException(e);
		}

	}


}
