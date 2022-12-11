package br.com.caipora.eventos.v1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caipora.eventos.v1.exceptions.ErroNegocialException;
import br.com.caipora.eventos.v1.exceptions.ErroSqlException;
import br.com.caipora.eventos.v1.exceptions.ErrosSistema;
import br.com.caipora.eventos.v1.models.Evento;
import br.com.caipora.eventos.v1.models.EventoComunicacao;
import br.com.caipora.eventos.v1.models.Topico;
import io.agroal.api.AgroalDataSource;

@ApplicationScoped
public class EventosDAO {

    private static final int ENV_QTD_MAX_RECORRENCIA = ConfigProvider.getConfig()
            .getValue("caipora.broker.limite.qtd.recorrencia", Integer.class);

    private static final int TEMPO_MAXIMO_PENDENCIAS_PROCESSAMENTO_SEGUNDOS = ConfigProvider.getConfig()
            .getValue("caipora.broker.limite.tempo.segundos.limpa.pendentes.processamento", Integer.class);

    private static final int TEMPO_KEEP_ALIVE = ConfigProvider.getConfig()
            .getValue("caipora.broker.limite.tempo.segundos.keepalive", Integer.class);

    private static final int _TAMANHO_PAGINACAO = ConfigProvider.getConfig()
            .getValue("caipora.broker.tamanho.paginacao.evento", Integer.class);

    private static final String _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO = "I";
    private static final String _ESTADO_PROCESSAMENTO_ALOCADO = "P";

    private Logger logger = LoggerFactory.getLogger(EventosDAO.class);

    @Inject
    AgroalDataSource defaultDataSource;

    @Inject
    BalanceamentoConsumidoresDAO balanceamentoConsumidoresDao;

    private Long gerarNovoSequencial() throws ErroNegocialException {
        logger.trace("CaiporaEventosDao.gerarNovoSequencial()");
        String sqlAtualizaGrupo = new StringBuilder()
                .append("SELECT CONCAT(")
                .append("EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::TEXT,")
                .append("EXTRACT(DOY FROM CURRENT_TIMESTAMP)::TEXT,")
                .append("LPAD(nextval('caipora.evento_sequence')::TEXT, 10, '0'));")
                .toString();

        Long sequencial = null;
        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);
                ResultSet rs = prepareStatement.executeQuery();) {
            if (rs.next()) {
                sequencial = rs.getLong(1);
                logger.trace("Sequencial: " + sequencial);
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

    public EventoComunicacao inserirEvento(Evento evento)
            throws ErroNegocialException {
        logger.trace("CaiporaEventosDao.inserirEvento()");

        avaliaConfiguracaoTopico(evento.getTopico());

        Long offset = this.gerarNovoSequencial();
        int rowsCreated = 0;

        String sqlInserirEvento = "INSERT INTO caipora.evento_original_topico\n" +
                "(tx_id_topico, id_evento_offset, ts_inclusao_registro, payload)\n" +
                "VALUES(?, ?,  CURRENT_TIMESTAMP, ?);\n";

        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlInserirEvento);) {
            prepareStatement.setString(1, evento.getTopico());
            prepareStatement.setLong(2, offset);
            prepareStatement.setString(3, evento.getPayload());
            rowsCreated = prepareStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erro em inserirEvento() ", e);
            throw new ErroSqlException(e);
        }
        if (rowsCreated == 0) {
            throw new ErroNegocialException(ErrosSistema.ERRO_AO_INSERIR_EVENTO.get());
        }
        return new EventoComunicacao(evento.getTopico(), offset, _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO, null, null, evento.getPayload());
    }

    private void avaliaConfiguracaoTopico(String topicoId) throws ErroNegocialException {

        // avalia se existe o topico
        // se existe verifica se esta ativo
        String sqlEncontrarEvento = "SELECT "
                + "tx_id_topico, descricao_topico, indicador_ativo\n" +
                "FROM "
                + "caipora.conf_topico\n"
                + "Where tx_id_topico = ?";

        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement p1 = con.prepareStatement(sqlEncontrarEvento);) {
            p1.setString(1, topicoId);
            Topico topico = null;
            try (ResultSet rs = p1.executeQuery();) {
                while (rs.next()) {
                    topico = new Topico(
                            rs.getString("tx_id_topico"),
                            rs.getString("descricao_topico"),
                            rs.getString("indicador_ativo"));
                }
            }
            if (topico != null) {
                if (topico.getIndicadorAtivo().equalsIgnoreCase("N")) {
                    throw new ErroNegocialException(ErrosSistema.ERRO_NEGOCIAL_TOPICO_INATIVO.get());
                }
            } else {
                // senao existe cria e ativa
                String sqlInsertTopico = "INSERT INTO caipora.conf_topico\n" +
                        "(tx_id_topico, descricao_topico, indicador_ativo)\n" +
                        "VALUES(?, 'Topico gerado automaticamente. Favor detalhar descrição a posteriori', 'S'::bpchar)";
                try (PreparedStatement p2 = con.prepareStatement(sqlInsertTopico);) {
                    p2.setString(1, topicoId);
                    p2.execute();
                }
            }

        } catch (SQLException e) {
            logger.error("Erro em avaliaConfiguracaoTopico() ", e);
            throw new ErroSqlException(e);
        }

    }

    public EventoComunicacao buscarProximo(String textoBindTopico, String idExecutor, long particaoSubscricao, long fatorMod)
            throws ErroNegocialException {

        
        removerPendenciasProcessamento(textoBindTopico, idExecutor,
                    TEMPO_MAXIMO_PENDENCIAS_PROCESSAMENTO_SEGUNDOS);
        
        EventoComunicacao eventoGrupo = buscarProximoTabelaEventoGrupo(textoBindTopico, idExecutor, particaoSubscricao, fatorMod);
        if (eventoGrupo == null) {
            int paginacaoConsumidores = _TAMANHO_PAGINACAO;
            logger.trace("paginacaoConsumidores=" + paginacaoConsumidores);
            carregarTabelaEventoGrupo(textoBindTopico, idExecutor, paginacaoConsumidores);
            eventoGrupo = buscarProximoTabelaEventoGrupo(textoBindTopico, idExecutor, particaoSubscricao, fatorMod);

        }

        if (eventoGrupo != null) {
            updateRecorrencia(eventoGrupo);
            updateProcessandoEvento(eventoGrupo, idExecutor);
        }

        return eventoGrupo;
    }
    
    private void updateProcessandoEvento(EventoComunicacao payloadEvento, String idConsumidor) throws ErroSqlException, ErroNegocialException {
        logger.trace("updateProcessandoEvento");
        
        logger.trace(payloadEvento.toString());
        
        String sql = "UPDATE caipora.evento_consumidor_topico SET id_consumidor = '"+idConsumidor+"',estado_processamento = '"+_ESTADO_PROCESSAMENTO_ALOCADO+"' ,tx_descricao_atualizacao = 'Evento alocado no cliente', ts_ultima_atualizacao = current_timestamp WHERE tx_id_topico_bind = ? and id_evento_offset = ?";
        
        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sql);) {
            
            prepareStatement.setString(1, payloadEvento.getEvento().getTopico());
            prepareStatement.setLong(2, payloadEvento.getOffsetEvento());
            
            prepareStatement.execute();
            
        } catch (SQLException e) {
            
            logger.error("sql="+sql, e);
            throw new ErroSqlException(e);
        }
    }


    public void removerPendenciasProcessamento(String textoBindTopico, String idExecutor, int tempoPendenciaSegundos)
            throws ErroNegocialException {
        logger.trace("removerPendenciasProcessamento");
        if (balanceamentoConsumidoresDao.ehOLider(textoBindTopico, idExecutor, TEMPO_KEEP_ALIVE)) {
        String sqlLimpa = 
                " UPDATE caipora.evento_consumidor_topico "
                + " SET id_consumidor = '"+idExecutor+"', estado_processamento = '"+_ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO+"' ,tx_descricao_atualizacao = 'Redisponibilizar evento.', ts_ultima_atualizacao = current_timestamp "
                + " WHERE tx_id_topico_bind = '"+textoBindTopico+"' AND id_evento_offset IN ("
                + "     SELECT  id_evento_offset FROM caipora.evento_consumidor_topico WHERE estado_processamento = '"+_ESTADO_PROCESSAMENTO_ALOCADO+"' AND tx_id_topico_bind = '"+textoBindTopico+"' AND"
                + DB2_H2_Utils.timestampSuperaTempoDeterminado("ts_ultima_atualizacao", tempoPendenciaSegundos)
                + " FETCH FIRST 1000 ROWS ONLY )";

        logger.trace( sqlLimpa);
        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlLimpa);) {
//            prepareStatement.setString(1, textoBindTopico);
            con.setAutoCommit(false);
            int executeUpdate = prepareStatement.executeUpdate();
            if (executeUpdate == 1) {
                logger.trace("evento=LIMPOU_1000_PENDENTES_PROCESSAMENTO,configuracao={textoBindTopico="
                        + textoBindTopico
                        + ",idExecutor=" + idExecutor + ",tempoPendenciaSegundos=" + tempoPendenciaSegundos + "}");
            }
            con.commit();

        } catch (SQLException e) {
            logger.error("sql=" + sqlLimpa, e);
            throw new ErroSqlException(e);
        }
        }

    }

    private void updateRecorrencia(EventoComunicacao eventoGrupo) throws ErroNegocialException {
        logger.trace("updateEstadoTratamentoParaPendenteConsumidor");
        String sqlAtualizaGrupo = "UPDATE caipora.evento_consumidor_topico SET recorrencia = recorrencia + 1  WHERE tx_id_topico_bind = ? and id_evento_offset = ? and  estado_processamento = ?";

        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);) {

            eventoGrupo.setRecorrencia(eventoGrupo.getRecorrencia() + 1);// feito aqui para evitar uma consulta sql

                prepareStatement.setString(1, eventoGrupo.getEvento().getTopico());
                prepareStatement.setLong(2, eventoGrupo.getOffsetEvento());
                prepareStatement.setString(3, _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO);

                prepareStatement.execute();

        } catch (SQLException e) {
            logger.error("Erro em updateRecorrencia()", e);
            throw new ErroSqlException(e);
        }
    }

    private void carregarTabelaEventoGrupo(String textoBindTopico, String idExecutor, int tamanhoPaginacao) throws ErroNegocialException {
        logger.trace(" >>>>> carregarTabelaEventoGrupo");

        // Elege id do leader
        String leader = balanceamentoConsumidoresDao.getLider_verificacaoCentralizadaNoDB(textoBindTopico,
                TEMPO_KEEP_ALIVE);
        logger.trace("\nid_executor=" + idExecutor + ", \nid_leader=  " + leader);
        // travar execucao pelo leader
        if (idExecutor.equals(leader)) {
            
            StringBuilder query = new StringBuilder();

            query.append("select\n");
            query.append("'" + textoBindTopico + "',origem.id_evento_offset, \n");
            query.append("'" + _ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO + "', now(), origem.payload\n");
            query.append("from\n");
            query.append("caipora.evento_original_topico as origem\n");
            query.append("where\n");
            query.append("origem.tx_id_topico like '" + textoBindTopico.replace('*', '%') + "' and\n");
            query.append("(\n");
            query.append("origem.id_evento_offset > (\n");
            query.append(
                    "select id_evento_offset from caipora.evento_consumidor_topico WHERE tx_id_topico_bind = '" + textoBindTopico
                            + "' \n");
            query.append("order by id_evento_offset desc limit 1\n");
            query.append(")\n");
            query.append(
                    "or not exists (select 1 from caipora.evento_consumidor_topico WHERE tx_id_topico_bind = '" + textoBindTopico
                            + "' )\n");
            query.append(")\n");

            query.append("order by origem.id_evento_offset asc\n");
            query.append("limit " + tamanhoPaginacao + "\n");
            
            String sqlReloadEventos = query.toString();

            logger.trace(sqlReloadEventos);

            try (Connection con = defaultDataSource.getConnection(); Statement psQuery = con.createStatement();) {

                List<EventoComunicacao> eventos = new ArrayList<EventoComunicacao>();
                try (ResultSet rs = psQuery.executeQuery(sqlReloadEventos);) {
                    while (rs.next()) {
                        eventos.add(new EventoComunicacao(  
                                rs.getString(1), rs.getLong(2), rs.getString(3), 
                                rs.getTimestamp(4), rs.getTimestamp(4),
                                rs.getString(5)));

                    }

                }

                if (eventos.size() > 0) {
                    long maxSeq = 0;
                    String sqlMaxSeq = " select MAX(sequencial_balanceamento)\n"
                            + " FROM caipora.evento_consumidor_topico\n"
                            + " where tx_id_topico_bind = '"+textoBindTopico+"'";
                    try (Statement psMaxSeq = con.createStatement();
                           ResultSet rsMaxSeq = psQuery.executeQuery(sqlMaxSeq );) {
                        while (rsMaxSeq.next()) {
                            maxSeq = rsMaxSeq.getLong(1);
                             

                        }
                    }

                    String sqlInsertEventoGrupo = "INSERT INTO caipora.evento_consumidor_topico  \n"
                            + "( tx_id_topico_bind, id_evento_offset, estado_processamento, recorrencia, ts_inclusao_registro_original, ts_inclusao_registro, payload ,sequencial_balanceamento) \n"
                            + "VALUES ( ?, ?, ?, 0, ?, now() , ?, ?)\n";

                    try (PreparedStatement psInsertEvento = con.prepareStatement(sqlInsertEventoGrupo);) {
                        con.setAutoCommit(false);

                        for (EventoComunicacao eventoGrupo : eventos) {
                            psInsertEvento.setString(1, eventoGrupo.getEvento().getTopico());
                            psInsertEvento.setLong(2, eventoGrupo.getOffsetEvento());
                            psInsertEvento.setString(3, eventoGrupo.getEstadoProcessamento());
                            psInsertEvento.setTimestamp(4, eventoGrupo.getTimestampInclusaoRegistro());
                            psInsertEvento.setString(5, eventoGrupo.getEvento().getPayload());
                            psInsertEvento.setLong(6, ++maxSeq);
                            psInsertEvento.addBatch();

                        }
                        psInsertEvento.executeBatch();

                        con.commit();
                    }
                }
            } catch (SQLException e1) {
                logger.error("Erro ao carregar tabela de eventos na tabela de evento do grupo", e1);
                throw new ErroSqlException(e1);
            }
        }

    }


    private EventoComunicacao buscarProximoTabelaEventoGrupo(String textoBindTopico, String idExecutor,
            long particaoSubscricao, long fatorMod)
            throws ErroNegocialException {

        String sqlDocumentosParaProcessamento = " SELECT "
                + " tx_id_topico_bind, id_evento_offset, estado_processamento, recorrencia, ts_inclusao_registro_original,ts_inclusao_registro, payload, sequencial_balanceamento   "
                + " FROM caipora.evento_consumidor_topico  WHERE "
                + " estado_processamento = '"+_ESTADO_PROCESSAMENTO_PRONTO_P_CONSUMO+"'"
                + " and tx_id_topico_bind = '"+textoBindTopico+"' and MOD(sequencial_balanceamento,"+fatorMod+") = "+particaoSubscricao+" "
                + " order by id_evento_offset asc limit 1 ";

        logger.info("QUERY NOVO EVENTO -->"+ sqlDocumentosParaProcessamento);
        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlDocumentosParaProcessamento);) {

            try (ResultSet rs = prepareStatement.executeQuery();) {
                EventoComunicacao evento = null;
                while (rs.next()) {
                    evento = new EventoComunicacao(rs.getString(1), rs.getLong(2),rs.getString(3),
                            rs.getInt(4), rs.getTimestamp(5),rs.getTimestamp(6),
                            rs.getString(7), rs.getLong(8),fatorMod, particaoSubscricao);
                    System.out.println("EventosDAO.buscarProximoTabelaEventoGrupo()"+evento);
                    
                    return evento;
                }
                return evento;
            }
        } catch (SQLException e) {
            logger.error("Erro em buscarProximoTabelaEventoGrupo()", e);
            throw new ErroSqlException(e);
        }

    }

    public void finalizarEvento(EventoComunicacao payloadEvento) throws ErroNegocialException {
        logger.trace("finalizarEvento");
        logger.info("FINALIZAR="+payloadEvento);
//        String sqlAtualizaFimProcessamento = "UPDATE DB2IMA.reg_exea_srtn_bch\n"
//                + "SET in_exea_srtn_bch=?, tx_rstd_exea_srtn = ? , ts_exea_srtn=current_timestamp \n"
//                + "WHERE nr_reg_exea_bch=? AND nr_gr_csmo_evt_im=? AND nr_dist_evt_sbst=?;";
//        try (Connection con = defaultDataSource.getConnection();
//                PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaFimProcessamento);) {
//
//            con.setAutoCommit(false);
//
//            prepareStatement.setString(1, (payloadEvento.getCodigoRetornoProcessamento() == 0) ? "S" : "Z");
//            prepareStatement.setString(2,
//                    (payloadEvento.getTextoMensagemRetornoProcessamento().length() > 99)
//                            ? payloadEvento.getTextoMensagemRetornoProcessamento().substring(0, 99)
//                            : payloadEvento.getTextoMensagemRetornoProcessamento());
//            prepareStatement.setLong(3, payloadEvento.getOffsetEvento());
//            prepareStatement.setString(4, payloadEvento.getEvento().getTopico());
//            prepareStatement.setLong(5, payloadEvento.getParticao());
//
//            prepareStatement.execute();
//            con.commit();
//
//        } catch (SQLException e) {
//            logger.error("sql=" + sqlAtualizaFimProcessamento + ",payloadEvento=" + payloadEvento.toString(), e);
//            throw new ErroSqlException(e);
//        }

    }

}
