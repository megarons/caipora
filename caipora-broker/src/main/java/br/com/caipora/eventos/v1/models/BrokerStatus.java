package br.com.caipora.eventos.v1.models;

import java.util.List;

public class BrokerStatus {

    private int qtdParticoes;
    private boolean estaBalanceado;
    private List<Subscricao> subscricoesAtivas;
    private String textoBindTopico;
    private int qtdSubscricoes;

    public BrokerStatus(String textoBindTopico, int qtdParticoes, boolean estaBalanceado, List<Subscricao> subscricoesAtivas) {
        this.textoBindTopico = textoBindTopico;
        this.qtdParticoes = qtdParticoes;
        this.estaBalanceado = estaBalanceado;
        this.subscricoesAtivas = subscricoesAtivas;
        this.qtdSubscricoes = (subscricoesAtivas == null)?0:subscricoesAtivas.size();
    }

    public int getQtdSubscricoes() {
        return qtdSubscricoes;
    }

    public void setQtdSubscricoes(int qtdSubscricoes) {
        this.qtdSubscricoes = qtdSubscricoes;
    }

    public String getTextoBindTopico() {
        return textoBindTopico;
    }

    public void setTextoBindTopico(String textoBindTopico) {
        this.textoBindTopico = textoBindTopico;
    }

    public int getQtdParticoes() {
        return qtdParticoes;
    }

    public void setQtdParticoes(int qtdParticoes) {
        this.qtdParticoes = qtdParticoes;
    }

    public boolean isEstaBalanceado() {
        return estaBalanceado;
    }

    public void setEstaBalanceado(boolean estaBalanceado) {
        this.estaBalanceado = estaBalanceado;
    }

    public List<Subscricao> getSubscricoesAtivas() {
        return subscricoesAtivas;
    }

    public void setSubscricoesAtivas(List<Subscricao> subscricoesAtivas) {
        this.subscricoesAtivas = subscricoesAtivas;
    }

}
