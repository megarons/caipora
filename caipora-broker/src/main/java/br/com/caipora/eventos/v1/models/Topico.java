package br.com.caipora.eventos.v1.models;

public class Topico {

    private String indicadorAtivo;
    private String descricao;
    private String id;

    public Topico(String id, String descricao, String indicadorAtivo) {
       this.id = id;
       this.descricao = descricao;
       this.indicadorAtivo = indicadorAtivo;
    }

    @Override
    public String toString() {
        return "Topico [indicadorAtivo=" + indicadorAtivo + ", descricao=" + descricao + ", id=" + id + "]";
    }


    public String getIndicadorAtivo() {
        return indicadorAtivo;
    }

    public void setIndicadorAtivo(String indicadorAtivo) {
        this.indicadorAtivo = indicadorAtivo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
