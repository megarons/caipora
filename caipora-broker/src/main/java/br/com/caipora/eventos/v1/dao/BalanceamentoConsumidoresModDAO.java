package br.com.caipora.eventos.v1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caipora.eventos.v1.exceptions.ComunicaConsumidorWarning;
import br.com.caipora.eventos.v1.exceptions.ErroNegocialException;
import br.com.caipora.eventos.v1.exceptions.ErroSqlException;
import br.com.caipora.eventos.v1.exceptions.ErrosSistema;
import br.com.caipora.eventos.v1.models.BrokerStatus;
import br.com.caipora.eventos.v1.models.Particao;
import br.com.caipora.eventos.v1.models.Subscricao;
import io.agroal.api.AgroalDataSource;

@ApplicationScoped
public class BalanceamentoConsumidoresModDAO {

    private Logger logger = LoggerFactory.getLogger(BalanceamentoConsumidoresModDAO.class);

    @Inject
    AgroalDataSource defaultDataSource;

    

    private void matarConfiguracoesClientesInativas(String textoBindTopico, String idExecutor,
            int tempoKeepAliveSegundos) throws ErroNegocialException {
        logger.trace("matarConfiguracoesClientesInativas");
        // Elege id do lider

        // travar execucao pelo lider
        if (ehOLider(textoBindTopico, idExecutor, tempoKeepAliveSegundos)) {
            int tempoLimiteInatividadeSegundos = tempoKeepAliveSegundos;
            String sqlDeletarSubscricao = "DELETE FROM caipora.subscricoes WHERE tx_id_topico_bind = '"
                    + textoBindTopico + "' AND " +
                    DB2_H2_Utils.timestampSuperaTempoDeterminado("ts_ultima_atividade", tempoLimiteInatividadeSegundos);
            try (Connection con = defaultDataSource.getConnection();
                    PreparedStatement prepareStatement = con.prepareStatement(sqlDeletarSubscricao);) {
                prepareStatement.execute();
            } catch (SQLException e) {
                logger.error("sql=" + sqlDeletarSubscricao, e);
                throw new ErroSqlException(e);
            }

            String sqlDeletarSubscricaoParticoes = "DELETE FROM caipora.subscricoes_particoes "
                    + "WHERE tx_id_topico_bind = '" + textoBindTopico
                    + "' AND id_executor NOT IN (select id_executor FROM caipora.subscricoes where tx_id_topico_bind = '"
                    + textoBindTopico + "')";
            try (Connection con = defaultDataSource.getConnection();
                    PreparedStatement prepareStatement = con.prepareStatement(sqlDeletarSubscricaoParticoes);) {
                prepareStatement.execute();
            } catch (SQLException e) {
                logger.error("sql=" + sqlDeletarSubscricaoParticoes);
                throw new ErroSqlException(e);
            }

        }

    }
    


    public int qtdConsumidoresTopico(String textoBindTopico) throws ErroNegocialException {
        String sqlContaConsumidoresTopico =
                  "   select "
                + "     count(distinct(id_executor)) "
                + " from "
                + "     caipora.subscricoes "
                + " where "
                + "     tx_id_topico_bind = ? ";
        int qtdConsumidores = 0;
        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlContaConsumidoresTopico);) {
            prepareStatement.setString(1, textoBindTopico);
            try (ResultSet rs = prepareStatement.executeQuery();) {
                while (rs.next()) {
                    qtdConsumidores = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("sql=" + sqlContaConsumidoresTopico, e);
            throw new ErroSqlException(e);
        }
        logger.debug("textoBindTopico=" + textoBindTopico + ",qtdConsumidores=" + qtdConsumidores);
        return (qtdConsumidores == 0 ? 1 : qtdConsumidores);

    }

    private Subscricao encontrarSubscricao(String textoBindTopico, String idExecutor, 
            int tempoKeepAliveSegundos) throws ErroNegocialException {
        logger.trace("encontrarSubscricao");
        String sqlEncontrarSubscricao = "SELECT tx_id_topico_bind, id_executor, ts_ultima_atividade FROM caipora.subscricoes WHERE tx_id_topico_bind = ? and id_executor = ? ";
        Subscricao subscricao = null;
        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarSubscricao);) {
            prepareStatement.setString(1, textoBindTopico);
            prepareStatement.setString(2, idExecutor);

            try (ResultSet rs = prepareStatement.executeQuery();) {
                while (rs.next()) {
                    subscricao = new Subscricao(rs.getString(1), rs.getString(2),
                            getParticoes(con, textoBindTopico, idExecutor, tempoKeepAliveSegundos), rs.getTimestamp(3));
                }
            }
        } catch (SQLException e) {
            logger.error("sql=" + sqlEncontrarSubscricao, e);
            throw new ErroSqlException(e);
        }

        if (subscricao == null) {
            
            return cadastrarMembro(textoBindTopico, idExecutor, tempoKeepAliveSegundos);
            

        }
        return subscricao;

    }

    private void atualizarUltimaAtividade(Subscricao subscricao) throws ErroNegocialException {
        synchronized (subscricao) {

            logger.trace("atualizarUltimaAtividade");
            String sqlAtualizaGrupo = "UPDATE caipora.subscricoes SET ts_ultima_atividade = current_timestamp WHERE tx_id_topico_bind = ? and id_executor = ? ";
            try (Connection con = defaultDataSource.getConnection();
                    PreparedStatement prepareStatement = con.prepareStatement(sqlAtualizaGrupo);) {
                prepareStatement.setString(1, subscricao.getStringDeBind());
                prepareStatement.setString(2, subscricao.getIdConsumidor());

                prepareStatement.execute();

            } catch (SQLException e) {
                logger.error("sql=" + sqlAtualizaGrupo, e);
                throw new ErroSqlException(e);
            }
        }

    }

    private void rebalancearParticoes(String textoBindTopico, String idConsumidor, 
            int tempoKeepAliveSegundos) throws ErroNegocialException {
        logger.debug("rebalancearParticoes");

        if (!estaBalanceado_verificacaoCentralizadaNoDB(textoBindTopico, idConsumidor,
                tempoKeepAliveSegundos)) {
            logger.debug(" GRUPO DESBALANCEADO !!!");
            // travar execucao pelo lider
            if (ehOLider(textoBindTopico, idConsumidor, tempoKeepAliveSegundos)) {

                limparBalanceamento(textoBindTopico);
                List<Subscricao> subscricoes = getSubscricoes(textoBindTopico,idConsumidor,tempoKeepAliveSegundos);

                logger.debug(" ANTES subscricoes=" + subscricoes);
                if (subscricoes != null && subscricoes.size() > 0) {

                    Object[][] resposta = new Object[subscricoes.size()][3];
                    // recalcular balanceamento

                    int ct = 0;
                    int limit = subscricoes.size();
                    for (int j = 0; j < subscricoes.size(); j++) {
                        Subscricao subscricaoObjeto = (Subscricao) subscricoes.get(ct);
                        resposta[j][0] = subscricaoObjeto.getStringDeBind();
                        resposta[j][1] = subscricaoObjeto.getIdConsumidor();
                        resposta[j][2] = j;
                        ct++;

                        if (ct == limit) {
                            ct = 0;
                        }

                    }

                    logger.debug(" DEPOIS subscricoes=" + subscricoes);

                    String sqlIncluirParticoes = "INSERT INTO caipora.subscricoes_particoes ( tx_id_topico_bind, id_executor, particao) VALUES (?, ?, ?)  ";
                    try (Connection con = defaultDataSource.getConnection();
                            PreparedStatement prepareStatement = con.prepareStatement(sqlIncluirParticoes);) {
                        con.setAutoCommit(false);
                        for (int j = 0; j < subscricoes.size(); j++) {

                            prepareStatement.setString(1, (String) resposta[j][0]);
                            prepareStatement.setString(2, (String) resposta[j][1]);
                            prepareStatement.setInt(3, ((int) resposta[j][2]));
                            prepareStatement.addBatch();

                        }
                        prepareStatement.executeBatch();
                        con.commit();

                    } catch (SQLException e) {
                        logger.error("sql=" + sqlIncluirParticoes, e);
                        throw new ErroSqlException(e);
                    }
                    logger.warn("evento=REBALANCEADO,executadoPor={textoBindTopico=" + textoBindTopico
                            + ",idConsumidor=" + idConsumidor.trim() + "},qtdConsumidoresAtivos=" + subscricoes.size()
                            + ",qtdMaximaParticoes=" + subscricoes.size() + ",consumidoresAtivos={" + subscricoes
                            + "}\n");
                } else {
//                    logger.info(" CADASTRA MEMBRO !!!!!!");
//                    cadastrarMembro(textoBindTopico, idConsumidor, tempoKeepAliveSegundos);
                }
            }

            throw new ComunicaConsumidorWarning(ErrosSistema.CC_REBALANCEANDO_CONSUMIDORES.get());

        } else {

            logger.debug(" ESTA BALANCEADO !!!");

        }

    }

    public boolean ehOLider(String textoBindTopico, String idExecutor, int tempoKeepAliveSegundos)
            throws ErroNegocialException {
        logger.trace("ehOLider");
        String lider = getLider_verificacaoCentralizadaNoDB(textoBindTopico, tempoKeepAliveSegundos);
        
        logger.debug(
                ((idExecutor.equals(lider)) ? ">>>LIDER<<<" : ">>>executor<<<") + ",textoBindTopico=" + textoBindTopico
                        + ",id=|" + idExecutor.trim() + "|, lider=|" + ((lider != null) ? lider.trim() : null));

        return idExecutor.equals(lider) || lider == null;
    }

    private Subscricao cadastrarMembro(String textoBindTopico, String idExecutor, 
            int tempoKeepAliveSegundos) throws ErroNegocialException {
            logger.trace("cadastrarMembro");
            incluirSubscricao(textoBindTopico, idExecutor);
            return encontrarSubscricao(textoBindTopico, idExecutor, tempoKeepAliveSegundos);
    }

    
    private List<Particao> getParticoes(Connection con, String textoBindTopico, String idExecutor,int tempoKeepAliveSegundos) throws SQLException, ErroNegocialException {
        logger.trace("getParticoes");
            List<Particao> lista = null;
            String sqlEncontrarSubscricao = "SELECT particao FROM caipora.subscricoes_particoes WHERE tx_id_topico_bind = ? and id_executor = ? ";
            try (PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarSubscricao);) {
                prepareStatement.setString(1, textoBindTopico);
                prepareStatement.setString(2, idExecutor);
    
                try (ResultSet rs = prepareStatement.executeQuery();) {
                    lista = new ArrayList<>();
                    while (rs.next()) {
                        lista.add(new Particao(textoBindTopico, idExecutor, rs.getInt(1)));
                    }
                }
            }
            logger.debug("Lista #### size=" + lista.size());
            return lista;
    }

    private void limparBalanceamento(String textoBindTopico) throws ErroNegocialException {
        logger.trace("limparBalanceamento");
        String sqlDeletarParticoes = "DELETE FROM caipora.subscricoes_particoes where tx_id_topico_bind = ?";
        logger.debug("BalanceamentoConsumidoresDAO.limparBalanceamento() " + sqlDeletarParticoes);
        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlDeletarParticoes);) {
            prepareStatement.setString(1, textoBindTopico);
            prepareStatement.execute();
        } catch (SQLException e) {
            logger.error("sql=" + sqlDeletarParticoes);
            throw new ErroSqlException(e);
        }
    }

    private void incluirSubscricao(String textoBindTopico, String idExecutor) throws ErroNegocialException {
        logger.trace("incluirSubscricao");
        String sqlIncluirSubscricao = "INSERT INTO caipora.subscricoes (tx_id_topico_bind, id_executor, ts_ultima_atividade) VALUES ( ?, ?, current_timestamp )  ";
        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlIncluirSubscricao);) {
            con.setAutoCommit(false);
            prepareStatement.setString(1, textoBindTopico);
            prepareStatement.setString(2, idExecutor);

            prepareStatement.execute();
            con.commit();
        } catch (SQLException e) {
            logger.error("sql=" + sqlIncluirSubscricao, e);
            throw new ErroSqlException(e);
        }
    }

    
    
    private List<Subscricao> getSubscricoes(String textoBindTopico, String idExecutor, int tempoKeepAliveSegundos) throws ErroNegocialException {
        logger.trace("getSubscricoes");
        
        if (ehOLider(textoBindTopico, idExecutor, tempoKeepAliveSegundos)) {
            
            
            logger.info("***** rever subscricoes *****");
            String sqlEncontrarMembrosGrupo = "SELECT * FROM caipora.subscricoes WHERE  tx_id_topico_bind = ? ";
            List<Subscricao> lista = null;
            try (Connection con = defaultDataSource.getConnection();
                    PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarMembrosGrupo);) {
                prepareStatement.setString(1, textoBindTopico);
    
                try (ResultSet rs = prepareStatement.executeQuery();) {
                    lista = new ArrayList<Subscricao>();
                    while (rs.next()) {
                        lista.add(new Subscricao(rs.getString(1), rs.getString(2), new ArrayList<>(), rs.getTimestamp(3)));
    
                    }
                }
            } catch (SQLException e) {
                logger.error("sql=" + sqlEncontrarMembrosGrupo, e);
                throw new ErroSqlException(e);
            }
            logger.info(" $$$$$ Quantidade de subscritos "+lista.size());
            return lista;
        }
        
        return null;
        
    }

    public Subscricao atualizaProvaDeVidaConsumidor(String textoBindTopico, String idExecutor,
            int tempoKeepAliveSegundos) throws ErroNegocialException {
        logger.trace("atualizaProvaDeVidaConsumidor");
       
        Subscricao subscricao = encontrarSubscricao(textoBindTopico, idExecutor, 
                tempoKeepAliveSegundos);
        atualizarUltimaAtividade(subscricao);

        return subscricao;

    }


    public String getLider_verificacaoCentralizadaNoDB(String textoBindTopico, int tempoKeepAliveSegundos)
            throws ErroNegocialException {
        logger.trace("getLider_verificacaoCentralizadaNoDB");
        String sqlEncontrarLider = "with "
                + "executores as ("
                + "     select id_executor from caipora.subscricoes_particoes where tx_id_topico_bind = ? group by id_executor   "
                + ")  "
                + " select "
                + "     a.id_executor from caipora.subscricoes as a, executores as b "
                + " where "
                + "     tx_id_topico_bind = ? and   a.id_executor = b.id_executor "
                + "and "
                + DB2_H2_Utils.timestampEstaContidoNoTempoDeterminado("a.ts_ultima_atividade", tempoKeepAliveSegundos)
                + " order by    id_executor asc  fetch first 1 rows only";
        String lider = null;
        try (Connection con = defaultDataSource.getConnection();
                PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarLider);) {
            prepareStatement.setString(1, textoBindTopico);
            prepareStatement.setString(2, textoBindTopico);

            try (ResultSet rs = prepareStatement.executeQuery();) {
                while (rs.next()) {
                    lider = rs.getString(1);

                }
            }
        } catch (SQLException e) {
            logger.error("sql=" + sqlEncontrarLider, e);
            throw new ErroSqlException(e);
        }
        
        logger.debug("LIDER POR PAREAMENTO DAS TABVELAD="+lider);
        
        if (lider == null) {
            // eleger lider da tabela de subscricao - condicao quando nao iniciou nenhum
            // subscritor ainda.

            String sqlEncontrarLiderSubscricao = "  select id_executor from caipora.subscricoes "
                    + "where "
                    + "tx_id_topico_bind = '" + textoBindTopico + "' AND "
                    + DB2_H2_Utils.timestampEstaContidoNoTempoDeterminado("ts_ultima_atividade", tempoKeepAliveSegundos)
                    + " fetch first 1 rows only";

            try (Connection con = defaultDataSource.getConnection();
                    PreparedStatement prepareStatement = con.prepareStatement(sqlEncontrarLiderSubscricao);
                    ResultSet rs = prepareStatement.executeQuery();) {

                while (rs.next()) {
                    lider = rs.getString(1);

                }
            } catch (SQLException e) {
                logger.error("sql=" + sqlEncontrarLiderSubscricao, e);
                throw new ErroSqlException(e);
            }

        }
        logger.debug("LIDER ELEITO !!!"+lider);
        return lider;
    }

    
    public boolean consumidoresGrupoEstaoBalanceados_verificacaoCentralizadaNoDB(String textoBindTopico,
            String idExecutor, int tempoKeepAliveSegundos)
            throws ErroNegocialException {
        logger.trace("consumidoresGrupoEstaoBalanceados_verificacaoCentralizadaNoDB");
        matarConfiguracoesClientesInativas(textoBindTopico, idExecutor, tempoKeepAliveSegundos);
        rebalancearParticoes(textoBindTopico, idExecutor, tempoKeepAliveSegundos);
        return estaBalanceado_verificacaoCentralizadaNoDB(textoBindTopico, idExecutor, tempoKeepAliveSegundos);

    }
    
    public boolean estaBalanceado_verificacaoCentralizadaNoDB(String textoBindTopico,
            String idExecutor, int tempoKeepAliveSegundos)
            throws ErroNegocialException { 
        logger.debug("consumidoresGrupoEstaoBalanceados_verificacaoCentralizadaNoDB");
            Integer qtdMaximaParticoes = qtdConsumidoresTopico(textoBindTopico);
            String query = "with  subcricao_executores as (\n"
                    + "select a.id_executor from \tcaipora.subscricoes_particoes a where  a.tx_id_topico_bind = ? group by id_executor  ),\n"
                    + "subscricoes_ativas as (\n"
                    + "select \ta.id_executor  from \tcaipora.subscricoes a where a.tx_id_topico_bind = ? ),\n"
                    + "diferentes as (\n"
                    + "select * from subscricoes_ativas a where not exists ( select 1 from subcricao_executores b where a.id_executor = b.id_executor)\n"
                    + "union all\n"
                    + "select * from subcricao_executores a where not exists ( select 1 from subscricoes_ativas  b where a.id_executor = b.id_executor)\n"
                    + "),\n"
                    + "particoesUtilizadas as (\n"
                    + "select count(-1) as qtdParticoes from caipora.subscricoes_particoes WHERE tx_id_topico_bind = ?\n"
                    + "),\n"
                    + "diferentesQtd as (\n"
                    + "select count(-1) as qtdDiferentes from diferentes\n"
                    + "),\n"
                    + "verificacaoParticao as (\n" 
                    + "    select\n" 
                    + "        SUM(particao) as numVerificacaoParticoes\n"  
                    + "    from\n" 
                    + "        caipora.subscricoes_particoes\n"  
                    + "    where\n" 
                    + "        tx_id_topico_bind = ? \n"  
                    + ")" 
                    + "select a.qtdDiferentes, b.qtdParticoes, c.numVerificacaoParticoes\n"  
                    + "from  "
                    + "diferentesQtd a,\n" 
                    + "    particoesUtilizadas b,\n"  
                    + "    verificacaoParticao c\n";
    
            int qtdDesbalanceados = 1;
            int qtdParticoes = 0;
            long numVerificacaoParticoes = 0;
    
            try (Connection con = defaultDataSource.getConnection();
                    PreparedStatement prepareStatement = con.prepareStatement(query);) {
                prepareStatement.setString(1, textoBindTopico);
                prepareStatement.setString(2, textoBindTopico);
                prepareStatement.setString(3, textoBindTopico);
                prepareStatement.setString(4, textoBindTopico);
    
                try (ResultSet rs = prepareStatement.executeQuery();) {
                    while (rs.next()) {
                        qtdDesbalanceados = rs.getInt(1);
                        qtdParticoes = rs.getInt(2);
                        numVerificacaoParticoes = rs.getLong(3);
                    }
                }
            } catch (SQLException e) {
                logger.error("sql=" + query, e);
                throw new ErroSqlException(e);
            }
            
            logger.debug("\n###############\nqtdAtivo=" + qtdDesbalanceados + ",\nqtdRegistrado="
                    + qtdParticoes + ",\nnumeroParticoesDisponiveis=" + qtdMaximaParticoes + ",\nnumVerificacaoParticoes="+numVerificacaoParticoes+"\n###############\n");
            
            if(!numeroVerificacaoParticaoCorresponde(qtdParticoes,numVerificacaoParticoes)) {
                logger.debug("ESTA DESBALANCEADO !!! SEQUENCIA ERRADA DE PARTICOES !!! qtdAtivo=" + qtdDesbalanceados + ",qtdRegistrado="
                        + qtdParticoes + ",numeroParticoesDisponiveis=" + qtdMaximaParticoes + ",numVerificacaoParticoes="+numVerificacaoParticoes);
                logger.debug(                "BalanceamentoConsumidoresDAO.consumidoresGrupoEstaoBalanceados_verificacaoCentralizadaNoDB() = FALSE");
                return false;
            }
    
            if (qtdDesbalanceados != 0 || (qtdParticoes != 0 && qtdParticoes != qtdMaximaParticoes)) {
                
                logger.debug("ESTA DESBALANCEADO !!! qtdAtivo=" + qtdDesbalanceados + ",qtdRegistrado="
                        + qtdParticoes + ",numeroParticoesDisponiveis=" + qtdMaximaParticoes);
                if (qtdDesbalanceados <= qtdMaximaParticoes) {
                    logger.debug(                "BalanceamentoConsumidoresDAO.consumidoresGrupoEstaoBalanceados_verificacaoCentralizadaNoDB() = FALSE");
                    return false;
                } else {
    
                        logger.info("!!!LIMPANDO DESBALANCEADOS qtdDesbalanceados=" + qtdDesbalanceados + ",qtdParticoes=" +
                                +qtdParticoes + ",numeroParticoes=" + qtdMaximaParticoes);
    
                        // todos balanceandos existem nos ativos mas nao esta balanceada a estrutura
                        // ainda
                        // vamos retirar os diferentes
                        String sqlRemoveDiferentes = "\n" +
                                "DELETE FROM caipora.subscricoes a\n" +
                                " WHERE a.id_executor IN (\n" +
                                "\n" +
                                "   select * from \n" +
                                "       (select a.id_executor  \n" +
                                "       from caipora.subscricoes a \n" +
                                "       where a.tx_id_topico_bind = ? \n" +
                                "       ) a \n" +
                                "   where not exists ( \n" +
                                "       select 1 from \n" +
                                "           ( select a.id_executor \n" +
                                "           from caipora.subscricoes_particoes a \n" +
                                "           where  a.tx_id_topico_bind = ? group by id_executor  \n" +
                                "           ) b \n" +
                                "       where a.id_executor = b.id_executor\n" +
                                "       )\n" +
                                "   union all\n" +
                                "   select * from \n" +
                                "       ( select a.id_executor \n" +
                                "       from caipora.subscricoes_particoes a \n" +
                                "       where  a.tx_id_topico_bind = ? group by id_executor  \n" +
                                "       ) a \n" +
                                "   where not exists ( \n" +
                                "       select 1 from \n" +
                                "           (select a.id_executor  \n" +
                                "           from caipora.subscricoes a \n" +
                                "           where a.tx_id_topico_bind = ? \n" +
                                "       )  b \n" +
                                "       where a.id_executor = b.id_executor\n" +
                                "       )\n" +
                                ")";
                        try (Connection con = defaultDataSource.getConnection();
                                PreparedStatement prepareStatement = con.prepareStatement(sqlRemoveDiferentes);) {
                            prepareStatement.setString(1, textoBindTopico);
                            prepareStatement.setString(2, textoBindTopico);
                            prepareStatement.setString(3, textoBindTopico);
                            prepareStatement.setString(4, textoBindTopico);
                            prepareStatement.execute();
                        } catch (SQLException e) {
                            logger.error("sql=" + sqlRemoveDiferentes, e);
                            throw new ErroSqlException(e);
                        }
                }
            }
            logger.debug("BalanceamentoConsumidoresDAO.consumidoresGrupoEstaoBalanceados_verificacaoCentralizadaNoDB() = TRUE");
            return true;

    }

    /**
     * Metodo verifica se as particoes estao na sequencia crescente conforme esperado. 
     * Serve para demonstrar desbalanceamento ainda que o numero de consumidores esteja correto.
     * 
     * Situacao correta
     * Ex: consumidores | particoes
     *          1           0
     *          2           1
     *          3           2
     * Situacao ERRADA
     * Ex: consumidores | particoes
     *          1           0
     *          2           1
     *          3           3   
       
     *  
     * @param qtdParticoes
     * @param numVerificacaoParticoes
     * @return
     */
    private boolean numeroVerificacaoParticaoCorresponde(int qtdParticoes, long numVerificacaoParticoes) {
        long somaParticoes = 0;
        for (int i = 0; i < qtdParticoes; i++) {
            somaParticoes = somaParticoes + i;
        }
        
        return numVerificacaoParticoes == somaParticoes;
    }


    public List<BrokerStatus> getStatus(String textoBindTopico) throws ErroNegocialException {
        List<BrokerStatus> brokerStatus = new ArrayList<BrokerStatus>();
//        int qtdConsumidoresTopico = qtdConsumidoresTopico(textoBindTopico);
//        if(textoBindTopico != null && !textoBindTopico.equals("")) {
//                if(qtdConsumidoresTopico(textoBindTopico) == 1) {
//                    brokerStatus.add(
//                        new BrokerStatus(
//                                textoBindTopico,
//                                qtdConsumidoresTopico,
//                                bufferStatusBalanceamento.get(textoBindTopico),
//                                bufferSubscritos.get(textoBindTopico))
//                        );
//                }
//                
//        }
        return brokerStatus;

    }

}
