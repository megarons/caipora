package br.com.caipora.eventos.v1.models;

public class Evento {


    public Evento() {
        super();
    }
    //dados evento
    private String topico;
    private String payload;
    public Evento(String topicoGrupo, String payload) {
       this.topico = topicoGrupo;
       this.payload = payload;
    }
    public String getTopico() {
        return topico;
    }
    public void setTopico(String topicoGrupo) {
        this.topico = topicoGrupo;
    }
    public String getPayload() {
        return payload;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }
    @Override
    public String toString() {
        return "Evento [topico=" + topico + ", payload=" + payload + "]";
    } 
    
}
