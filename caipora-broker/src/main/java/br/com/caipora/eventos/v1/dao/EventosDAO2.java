//package br.com.caipora.eventos.v1.dao;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Arrays;
//import java.util.List;
//
//import javax.enterprise.context.ApplicationScoped;
//import javax.inject.Inject;
//
//import org.eclipse.microprofile.config.ConfigProvider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import br.com.caipora.eventos.v1.exceptions.ErroNegocialException;
//import br.com.caipora.eventos.v1.exceptions.ErroSqlException;
//import br.com.caipora.eventos.v1.exceptions.ErrosSistema;
//import br.com.caipora.eventos.v1.models.Evento;
//import br.com.caipora.eventos.v1.models.EventoComunicacao;
//import br.com.caipora.eventos.v1.models.Particao;
//import br.com.caipora.eventos.v1.models.Topico;
//import io.agroal.api.AgroalDataSource;
//
//@ApplicationScoped
//public class EventosDAO2 {
//
//	private static final int ENV_QTD_MAX_RECORRENCIA = ConfigProvider.getConfig().getValue("caipora.broker.limite.qtd.recorrencia",Integer.class);
//
//	private Logger logger = LoggerFactory.getLogger(EventosDAO2.class);
//
//	@Inject
//	AgroalDataSource defaultDataSource;
//
//	@Inject
//	BalanceamentoConsumidoresDAO balanceamentoConsumidoresDAO;
//
//	private String particaoToSQL(List<Particao> particao) {
//		StringBuilder str = new StringBuilder();
//		for (Particao registro : particao) {
//			str.append(registro.getParticao()).append(", ");
//		}
//
//		String particoesSQL = (!str.toString().equals("")) ? str.toString().substring(0, str.toString().length() - 2)
//				: "";
//		return particoesSQL;
//	}
//	
//    private Long gerarNovoSequencial() throws ErroNegocialException {
//        logger.trace("CaiporaEventosDao.gerarNovoSequencial()");
//        String sqlAtualizaGrupo = new StringBuilder()
//                .append("SELECT CONCAT(")
//                .append("EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::TEXT,")
//                .append("EXTRACT(DOY FROM CURRENT_TIMESTAMP)::TEXT,")
//                .append("LPAD(nextval('caipora.evento_sequence')::TEXT, 10, '0'));")
//                .toString();
//
//        Long sequencial = null;
//        try (Connection con = defaultDataSource.getConnection();
//                PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);
//                ResultSet rs = prepareStatement.executeQuery();) {
//            if (rs.next()) {
//                sequencial = rs.getLong(1);
//                logger.info("Sequencial: " + sequencial);
//            }
//            if (sequencial == null) {
//                throw new IllegalStateException("Valor do sequencial é nulo");
//            }
//        } catch (SQLException e) {
//            logger.error("Erro em gerarNovoSequencial() inativos", e);
//            throw new ErroSqlException(e);
//        }
//        return sequencial;
//    }
//    
//    public EventoComunicacao inserirEvento(Evento evento)
//            throws ErroNegocialException {
//        logger.trace("CaiporaEventosDao.inserirEvento()");
//        
//        avaliaConfiguracaoTopico(evento.getTopico());
//
//        Long sequencial = this.gerarNovoSequencial();
//        int rowsCreated = 0;
//
//        String sqlInserirEvento = 
//                "INSERT INTO caipora.evento_original_topico\n" + 
//                "(tx_id_topico, id_evento_offset, particao, ts_inclusao_registro, payload)\n" + 
//                "VALUES(?, ?, 0, CURRENT_TIMESTAMP, ?);\n";
//
//        try (Connection con = defaultDataSource.getConnection();
//                PreparedStatement prepareStatement = con.prepareStatement(sqlInserirEvento);) {
//            prepareStatement.setString(1, evento.getTopico());
//            prepareStatement.setLong(2, sequencial);
//            prepareStatement.setString(3, evento.getPayload());
//            rowsCreated = prepareStatement.executeUpdate();
//        } catch (SQLException e) {
//            logger.error("Erro em inserirEvento() ", e);
//            throw new ErroSqlException(e);
//        }
//        if (rowsCreated == 0) {
//            throw new ErroNegocialException(ErrosSistema.ERRO_AO_INSERIR_EVENTO.get());
//        }
//        return new EventoComunicacao(evento.getTopico(), sequencial, 0, "N", 0, null, null, evento.getPayload());
//    }
//
//	private void avaliaConfiguracaoTopico(String topicoId) throws ErroNegocialException {
//	    
//        //avalia se existe o topico
//	    //se existe verifica se esta ativo
//	    String sqlEncontrarEvento = 
//	            "SELECT "
//	            + "tx_id_topico, descricao_topico, indicador_ativo\n" + 
//	            "FROM "
//	            + "caipora.conf_topico\n"
//	            + "Where tx_id_topico = ?";
//	    
//	    try (Connection con = defaultDataSource.getConnection();
//                PreparedStatement p1 = con.prepareStatement(sqlEncontrarEvento);
//	                ){
//	            p1.setString(1, topicoId);
//	            Topico topico = null;
//	            try(ResultSet rs = p1.executeQuery();){
//                while (rs.next()) {
//                    topico = new Topico(
//                                rs.getString("tx_id_topico"),
//                                rs.getString("descricao_topico"),
//                                rs.getString("indicador_ativo")
//                            );
//                }}
//                if(topico != null) {
//                    if(topico.getIndicadorAtivo().equalsIgnoreCase("N")) {
//                        throw new ErroNegocialException(ErrosSistema.ERRO_NEGOCIAL_TOPICO_INATIVO.get());
//                    }
//                }else {
//                    //senao existe cria e ativa
//                    String sqlInsertTopico = 
//                            "INSERT INTO caipora.conf_topico\n" + 
//                            "(tx_id_topico, descricao_topico, indicador_ativo)\n" + 
//                            "VALUES(?, 'Topico gerado automaticamente. Favor detalhar descrição a posteriori', 'S'::bpchar)";
//                    try(PreparedStatement p2 = con.prepareStatement(sqlInsertTopico);){
//                        p2.setString(1, topicoId);
//                        p2.execute();
//                    }
//                }
//             
//        } catch (SQLException e) {
//            logger.error("Erro em avaliaConfiguracaoTopico() ", e);
//            throw new ErroSqlException(e);
//        }
//        
//    }
//
////    public EventoComunicacao buscarProximo(String idGrupo, String idConsumidor, List<Particao> particoes)
////			throws ErroNegocialException {
////
////		EventoComunicacao payloadEvento = null;
////		payloadEvento = carregarEventoDisponivelGrupo(idGrupo, idConsumidor, particoes);
////		if (payloadEvento != null) {
////			if(ENV_QTD_MAX_RECORRENCIA >= 1 && payloadEvento.getRecorrencia() >= ENV_QTD_MAX_RECORRENCIA ) {
////				updateEventoErro(payloadEvento, idConsumidor);
////				return buscarProximo(idGrupo, idConsumidor, particoes);
////				
////			}else {
////				updateProcessandoEvento(payloadEvento,idConsumidor);
////				updateRecorrencia(payloadEvento);
////				
////			}
////		}
////
////		return payloadEvento;
////	}
////	
////	private void updateProcessandoEvento(EventoComunicacao payloadEvento, String idConsumidor) throws ErroSqlException, ErroNegocialException {
////		logger.trace("updateProcessandoEvento");
////		
////		logger.info(payloadEvento.toString());
////		
////		String sql = "UPDATE DB2IMA.reg_exea_srtn_bch SET in_exea_srtn_bch = 'P' ,tx_rstd_exea_srtn = 'Evento alocado em "+idConsumidor+" ', ts_exea_srtn = current_timestamp WHERE nr_gr_csmo_evt_im = ? and nr_dist_evt_sbst = ? and nr_reg_exea_bch = ?";
////		
////		try (Connection con = defaultDataSource.getConnection();
////				PreparedStatement prepareStatement = con.prepareStatement(sql);) {
////			
////			prepareStatement.setString(1, payloadEvento.getEvento().getTopico());
////			prepareStatement.setLong(2, payloadEvento.getParticao());
////			prepareStatement.setLong(3, payloadEvento.getOffsetEvento());
////			
////			prepareStatement.execute();
////			
////		} catch (SQLException e) {
////			
////			logger.error("sql="+sql, e);
////			throw new ErroSqlException(e);
////		}
////	}
////	
////	private void updateRecorrencia(EventoComunicacao eventoGrupo) throws ErroNegocialException {
////		
////		logger.trace("updateRecorrencia");
////		String sqlAtualizaGrupo = "UPDATE DB2IMA.reg_exea_srtn_bch SET nr_tnt_prct = nr_tnt_prct + 1 , ts_exea_srtn = current_timestamp WHERE nr_gr_csmo_evt_im = ? and nr_dist_evt_sbst = ? and nr_reg_exea_bch = ? ";
////
////		try (Connection con = defaultDataSource.getConnection();
////				PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);) {
////
////			prepareStatement.setString(1, eventoGrupo.getEvento().getTopico());
////			prepareStatement.setLong(2, eventoGrupo.getParticao());
////			prepareStatement.setLong(3, eventoGrupo.getOffsetEvento());
////
////			prepareStatement.execute();
////
////		} catch (SQLException e) {
////			logger.error("sql="+sqlAtualizaGrupo, e);
////			throw new ErroSqlException(e);
////		}
////	}
////
////	private void updateEventoErro(EventoComunicacao payloadEvento,String idConsumidor) throws ErroSqlException, ErroNegocialException {
////		
////		String sqlAtualizaGrupo = "UPDATE DB2IMA.reg_exea_srtn_bch SET in_exea_srtn_bch = 'E', tx_rstd_exea_srtn = 'Limite de recorrencia atingido por "+idConsumidor+"', ts_exea_srtn = current_timestamp WHERE nr_gr_csmo_evt_im = ? and nr_dist_evt_sbst = ? and nr_reg_exea_bch = ? ";
////
////		try (Connection con = defaultDataSource.getConnection();
////				PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);) {
////			con.setAutoCommit(false);
////			prepareStatement.setString(1, payloadEvento.getEvento().getTopico());
////			prepareStatement.setLong(2, payloadEvento.getParticao());
////			prepareStatement.setLong(3, payloadEvento.getOffsetEvento());
////
////			int executeUpdate = prepareStatement.executeUpdate();
////			if(executeUpdate ==1) {
////				logger.warn("evento=SETA_ESTADO_ERRO_POR_RECORRENCIA,offset="+payloadEvento.getOffsetEvento()+",ultimoCliente={idGrupo="+payloadEvento.getEvento().getTopico()+",idConsumidor="+((idConsumidor!=null)?idConsumidor.trim():null)+"}");
////			}
////			con.commit();
////
////
////		} catch (SQLException e) {
////			logger.error("sql="+sqlAtualizaGrupo, e);
////			throw new ErroSqlException(e);
////		}
////		
////	}
////
////	private EventoComunicacao carregarEventoDisponivelGrupo(String grupo, String idConsumidor, List<Particao> particoes)
////			throws ErroSqlException, ErroNegocialException {
////		logger.trace("carregarEventoDisponivelGrupo");
////		if(particoes.size() == 0) {
////			throw new NullPointerException("Não há partição para o evento.");
////		}
////		String sqlEventoConsumidor = "select *  from DB2IMA.reg_exea_srtn_bch where \n"
////				+ "	cd_tip_prct_srtn = 'E' and in_exea_srtn_bch = 'N' and \n"
////				+ "	nr_gr_csmo_evt_im  = ? and nr_dist_evt_sbst in ( " + particaoToSQL(particoes) + " ) fetch first 1 rows only";
////
////		EventoComunicacao payloadEvento = null;
////		try (Connection con = defaultDataSource.getConnection();
////				PreparedStatement prepareStatement = con.prepareStatement(sqlEventoConsumidor);) {
////			prepareStatement.setString(1, grupo);
////
////			try (ResultSet rs = prepareStatement.executeQuery();) {
////				while (rs.next()) {
////					int operacao = rs.getInt("cd_opr");
////					int vrsOperacao = rs.getInt("nr_vrs_opr");
////					payloadEvento = new EventoComunicacao(
////							rs.getString("nr_gr_csmo_evt_im"), 		// grupo
////							rs.getLong("nr_reg_exea_bch"), 			// offsetEvento
////							rs.getInt("nr_dist_evt_sbst"), 			// particao
////							rs.getString("in_exea_srtn_bch"), 		// estadoProcessamento
////							rs.getInt("nr_tnt_prct"), 				// recorrencia,
////							rs.getTimestamp("ts_reg"), 				// timestampInclusaoRegistro,
////							rs.getTimestamp("ts_exea_srtn"), 		// timestampUltimaAtualizacao,
////							"" + ((operacao == 0)?"":operacao)
////					);
////
////				}
////			}
////
////			return payloadEvento;
////
////		} catch (SQLException e) {
////			logger.error(
////					"sql="+sqlEventoConsumidor+",grupo="+grupo+",idConsumidor="+idConsumidor+",particoes="+Arrays.toString(particoes.toArray()),
////					e);
////			throw new ErroSqlException(e);
////		}
////	}
////
////	public void finalizarEvento(EventoComunicacao payloadEvento) throws ErroNegocialException {
////		logger.trace("finalizarEvento");
////
////		String sqlAtualizaFimProcessamento = "UPDATE DB2IMA.reg_exea_srtn_bch\n"
////				+ "SET in_exea_srtn_bch=?, tx_rstd_exea_srtn = ? , ts_exea_srtn=current_timestamp \n"
////				+ "WHERE nr_reg_exea_bch=? AND nr_gr_csmo_evt_im=? AND nr_dist_evt_sbst=?;";
////		try (Connection con = defaultDataSource.getConnection();
////				PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaFimProcessamento);) {
////
////			con.setAutoCommit(false);
////			
////			prepareStatement.setString(1, (payloadEvento.getCodigoRetornoProcessamento() == 0)?"S":"Z");
////			prepareStatement.setString(2, (payloadEvento.getTextoMensagemRetornoProcessamento().length()>99)?payloadEvento.getTextoMensagemRetornoProcessamento().substring(0, 99):payloadEvento.getTextoMensagemRetornoProcessamento());
////			prepareStatement.setLong(3, payloadEvento.getOffsetEvento());
////			prepareStatement.setString(4, payloadEvento.getEvento().getTopico());
////			prepareStatement.setLong(5, payloadEvento.getParticao());
////
////			prepareStatement.execute();
////			con.commit();
////
////		} catch (SQLException e) {
////			logger.error("sql="+sqlAtualizaFimProcessamento+",payloadEvento="+payloadEvento.toString(), e);
////			throw new ErroSqlException(e);
////		}
////
////	}
////
////	public void removerPendenciasProcessamento(String idGrupo,String idExecutor, int tempoPendenciaSegundos) throws ErroNegocialException {
////		logger.trace("removerPendenciasProcessamento");
////
////		String sql = "UPDATE DB2IMA.reg_exea_srtn_bch "
////				+ " SET in_exea_srtn_bch='N', ts_exea_srtn=current_timestamp "
////				+ " WHERE NR_REG_EXEA_BCH IN ( "
////				+ " SELECT NR_REG_EXEA_BCH FROM DB2IMA.reg_exea_srtn_bch  WHERE in_exea_srtn_bch='P' AND nr_gr_csmo_evt_im=? AND"
////				+  DB2_H2_Utils.timestampSuperaTempoDeterminado("ts_exea_srtn",tempoPendenciaSegundos)
////				+ " FETCH FIRST 1000 ROWS ONLY )" ; 
////
////		
////		try (Connection con = defaultDataSource.getConnection();
////				PreparedStatement prepareStatement = con.prepareStatement(sql);) {
////			prepareStatement.setString(1, idGrupo);
////			con.setAutoCommit(false);
////			int executeUpdate = prepareStatement.executeUpdate();
////			if(executeUpdate == 1) {
////				logger.warn("evento=LIMPOU_1000_PENDENTES_PROCESSAMENTO,configuracao={idGrupo="+idGrupo+",idExecutor="+idExecutor+",tempoPendenciaSegundos="+tempoPendenciaSegundos+"}");
////			}
////			con.commit();
////
////		} catch (SQLException e) {
////			logger.error("sql="+sql, e);
////			throw new ErroSqlException(e);
////		}
////		
////	}
//	
//	
//	
//    public EventoComunicacao buscarProximo(int textoBindTopico, String idExecutor, List<Particao> particoes,
//            int registroPorPagina, int entrega, int estado, int tipologia) throws ErroNegocialException {
//
//        EventoComunicacao eventoGrupo = buscarProximoTabelaEventoGrupo(textoBindTopico, idExecutor, particoes);
//        if (eventoGrupo == null) {
//            int paginacaoConsumidores = registroPorPagina;
//            logger.info("paginacaoConsumidores=" + paginacaoConsumidores);
//            carregarTabelaEventoGrupo(textoBindTopico, idExecutor, paginacaoConsumidores, entrega, estado, tipologia);
//            eventoGrupo = buscarProximoTabelaEventoGrupo(textoBindTopico, idExecutor, particoes);
//
//        }
//
//        if (eventoGrupo != null) {
//            updateRecorrencia(eventoGrupo);
//        }
//
//        return eventoGrupo;
//    }
//
//private void updateRecorrencia(EventoComunicacao eventoGrupo) throws ErroNegocialException {
//logger.trace("updateEstadoTratamentoParaPendenteConsumidor");
//String sqlAtualizaGrupo = "UPDATE \"eventos\".eventos_grupo SET recorrencia = recorrencia + 1  WHERE cd_grupo = ? and particao = ? and id_evento_offset = ? and  estado_processamento = ?";
//
//try (Connection con = defaultDataSource.getConnection();
//      PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);) {
//  
//  eventoGrupo.setRecorrencia(eventoGrupo.getRecorrencia()+1);// feito aqui para evitar uma consulta sql 
//  
//  try (PreparedStatement statement = atualizaHistorico(con, eventoGrupo, _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO);) {
//      prepareStatement.setInt(1, eventoGrupo.getIdGrupo());
//      prepareStatement.setLong(2, eventoGrupo.getParticao());
//      prepareStatement.setLong(3, eventoGrupo.getId_evento_offset());
//      prepareStatement.setInt(4, _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO);
//
//      prepareStatement.execute();
//  }
//
//} catch (SQLException e) {
//  logger.error("Erro em updateRecorrencia()", e);
//  throw new ErroSqlException(e);
//}
//}
////
////private PreparedStatement atualizaHistorico(Connection con, EventoComunicacao eventoGrupo, int estado) throws SQLException {
////String sqlAtualizaHistoricoGrupo;
////  sqlAtualizaHistoricoGrupo = "INSERT INTO \"eventos\".historico_eventos_grupo "
////      + "("
////      + "cd_grupo,id_evento_offset,ts_atualizacao_evento,particao,estado_processamento,recorrencia,ts_inclusao_registro,ts_finalizacao_registro,ptl,entrega,estado,tipologia)"
////      + "VALUES "
////      + "(?,?,now(),?,?,?,?,?,?,?,?,? )";
////  
////  
////  try(PreparedStatement psHist = con.prepareStatement(sqlAtualizaHistoricoGrupo);){
////      psHist.setInt(1, eventoGrupo.getIdGrupo());
////      psHist.setLong(2, eventoGrupo.getId_evento_offset());
////      //3 now
////      psHist.setInt(3, eventoGrupo.getParticao());
////      psHist.setInt(4, estado);
////      psHist.setInt(5, eventoGrupo.getRecorrencia());
////      psHist.setTimestamp(6, eventoGrupo.getTimestampInclusaoRegistro());
////      psHist.setTimestamp(7,  new Timestamp(System.currentTimeMillis()));// mudar o insert para fazer o select na tabela evento_grupo ao inves de montar o objeto
////      psHist.setLong(8, eventoGrupo.getEvento().getProtocolo());
////      psHist.setInt(9, eventoGrupo.getEvento().getEntrega());
////      psHist.setLong(10, eventoGrupo.getEvento().getEstado());
////      psHist.setLong(11, eventoGrupo.getEvento().getTipologia());
////      
////      psHist.execute();
////      return psHist;
////  }
////
////}
////
//private void carregarTabelaEventoGrupo(int idGrupo, String idExecutor, int registrosPorPagina, int entrega, int estado,int tipologia) throws ErroNegocialException  {
//logger.trace(" >>>>> carregarTabelaEventoGrupo");
//
//// Elege id do leader
//String leader = getLeader_verificacaoCentralizadaNoDB(idGrupo);
//logger.info("\nid_executor=" + idExecutor + ", \nid_leader=  " + leader);
//// travar execucao pelo leader
//if (idExecutor.equals(leader)) {
//      String sqlReloadEventos = montaSqlReload(idGrupo, _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO,
//              "" + entrega, "" + estado,
//              "" + tipologia, registrosPorPagina);
//
//
//      logger.debug(sqlReloadEventos);
//
//      try (Connection con = defaultDataSource.getConnection(); Statement psQuery = con.createStatement();) {
//
//          List<EventoComunicacao> eventos = new ArrayList<EventoComunicacao>();
//          try (ResultSet rs = psQuery.executeQuery(sqlReloadEventos);) {
//              while (rs.next()) {
//                  
//                  eventos.add(new EventoComunicacao(rs.getInt(1), rs.getLong(4), rs.getInt(2), rs.getLong(3),
//                          rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(5), rs.getTimestamp(9)));
//              }
//
//          }
//
//          if (eventos.size() > 0) {
//              
//              String sqlInsertEventoGrupo = "INSERT INTO eventos.eventos_grupo  \n"
//                      + "( cd_grupo, id_evento_offset, particao,  estado_processamento, recorrencia, ts_inclusao_registro, ptl, entrega, estado, tipologia ) \n"
//                      + "VALUES ( ?, ?, ?, ?, 0, now() ,? ,? ,? , ? )\n";
//              
//              String sqlAtualizaHistoricoGrupo = "INSERT INTO \"eventos\".historico_eventos_grupo "
//                      + "( cd_grupo, id_evento_offset, ts_atualizacao_evento, particao, estado_processamento, recorrencia, ts_inclusao_registro, ptl, entrega, estado, tipologia)"
//                      + "VALUES "
//                      + "(?,?,now(),?,?,0, now(),?,?,?,? )";
//
//              try (PreparedStatement psInsertEvento = con.prepareStatement(sqlInsertEventoGrupo);) {
//              try(PreparedStatement psHist = con.prepareStatement(sqlAtualizaHistoricoGrupo);){
//                  con.setAutoCommit(false);
//
//                  for (EventoComunicacao eventoGrupo : eventos) {
//                      psInsertEvento.setInt(1, eventoGrupo.getIdGrupo());
//                      psInsertEvento.setLong(2, eventoGrupo.getId_evento_offset());
//                      psInsertEvento.setLong(3, eventoGrupo.getParticao());
//                      psInsertEvento.setInt(4, eventoGrupo.getEstado_processamento());
//                      psInsertEvento.setLong(5, eventoGrupo.getEvento().getProtocolo());
//                      psInsertEvento.setInt(6, eventoGrupo.getEvento().getEntrega());
//                      psInsertEvento.setInt(7, eventoGrupo.getEvento().getEstado());
//                      psInsertEvento.setInt(8, eventoGrupo.getEvento().getTipologia());
//                      psInsertEvento.addBatch();
//                      
//                      psHist.setInt(1, eventoGrupo.getIdGrupo());
//                      psHist.setLong(2, eventoGrupo.getId_evento_offset());
//                      //3 now
//                      psHist.setInt(3, eventoGrupo.getParticao());
//                      psHist.setInt(4, eventoGrupo.getEstado_processamento());
//
//                      
//                      psHist.setLong(5, eventoGrupo.getEvento().getProtocolo());
//                      psHist.setInt(6, eventoGrupo.getEvento().getEntrega());
//                      psHist.setLong(7, eventoGrupo.getEvento().getEstado());
//                      psHist.setLong(8, eventoGrupo.getEvento().getTipologia());
//                      
//                      psHist.addBatch();
//
//                  }
//                  psInsertEvento.executeBatch();
//                  psHist.executeBatch();
//
//                  con.commit();
//              }
//          }
//          }
//      } catch (SQLException e1) {
//          logger.error("Erro ao carregar tabela de eventos na tabela de evento do grupo", e1);
//          throw new ErroSqlException(e1);
//      }
//}
//
//}
////
////private String montaSqlReload(int cd_grupo, int estadoProcessamento, String entrega, String estado,
////  String tipologia, int registrosPorPagina) {
////StringBuilder query = new StringBuilder();
////
////query.append("select\n");
////query.append("'" + cd_grupo + "', origem.particao, origem.id_evento_offset ,origem.ptl,\n");
////query.append("'" + estadoProcessamento+ "', origem.entrega, origem.estado, origem.tipologia, now()\n");
////query.append("from\n");
////query.append("eventos.eventos_mensagens as origem\n");
////query.append("where\n");
////query.append("(\n");
////query.append("origem.id_evento_offset > (\n");
////query.append("select id_evento_offset from eventos.eventos_grupo WHERE cd_grupo = '" + cd_grupo + "' \n");
////query.append("order by id_evento_offset desc limit 1\n");
////query.append(")\n");
////query.append("or not exists (select 1 from eventos.eventos_grupo WHERE cd_grupo = '" + cd_grupo + "' )\n");
////query.append(")\n");
////if (entrega != null && !"".equals(entrega) && !"0".equals(entrega)) {
////  query.append("and origem.entrega = " + entrega + "\n");
////}
////if (estado != null && !"".equals(estado) && !"0".equals(estado)) {
////  query.append("and origem.estado = " + estado + "\n");
////}
////if (tipologia != null && !"".equals(tipologia) && !"0".equals(tipologia)) {
////  query.append("and origem.tipologia = " + tipologia + "\n");
////}
////query.append("order by origem.id_evento_offset asc\n");
////query.append("limit " + registrosPorPagina + "\n");
////return query.toString();
////}
////
//private EventoComunicacao buscarProximoTabelaEventoGrupo(int idGrupo, String idExecutor, List<Particao> particoes) throws ErroNegocialException
//   {
//String particoesSQL = particaoToSQL(particoes);
//
//logger.info("particoesSize="+particoes.size()+",particoes="+particoesSQL);
//String sqlDocumentosParaProcessamento = " SELECT "
//      + " cd_grupo, id_evento_offset, particao, estado_processamento, recorrencia, ts_inclusao_registro,  ptl,entrega, estado, tipologia   "
//      + " FROM eventos.eventos_grupo  WHERE "
//      + " estado_processamento = "+_ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO+" and cd_grupo = ? and particao in (" + particoesSQL
//      + ") order by id_evento_offset asc limit 1 ";
//
//try (Connection con = defaultDataSource.getConnection();
//      PreparedStatement prepareStatement = con.prepareStatement(sqlDocumentosParaProcessamento);) {
//  prepareStatement.setInt(1, idGrupo);
//
//  try (ResultSet rs = prepareStatement.executeQuery();) {
//      EventoComunicacao evento = null;
//      while (rs.next()) {
//          evento = new EventoComunicacao(rs.getInt(1), rs.getLong(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getTimestamp(6),
//                  rs.getLong(7), rs.getInt(8), rs.getInt(9), rs.getInt(10));
//          return evento;
//      }
//      return evento;
//  }
//} catch (SQLException e) {
//  logger.error("Erro em buscarProximoTabelaEventoGrupo()", e);
//  throw new ErroSqlException(e);
//}
//
//}
//
//
//}
