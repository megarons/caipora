package br.com.caipora.eventos.v1.models;

/**
 * Objeto com as configurações de uso de um consumidor do ima-eventos.
 * 
 * @author desenv
 *
 */
public class ConfiguracaoConsumidorSubscritor {

	// campos de identificação
	private int codigoIdentificadorGrupo;
	private String codigoIdentificadorUnicoConsumidor;

	// campos de configuração
	private int valorCicloTempoAtivo = 5;

	public ConfiguracaoConsumidorSubscritor() {
		super();
	}

	/**
	 * Instancia uma nova configuração de consumo. É obrigatorio um identificador
	 * para o consumidor. A identificação distinta entre os consumidores é o que
	 * permite o calculo de balanceamento de eventos distinto entre os consumidores.
	 * 
	 * @param idExecutor
	 */
	public ConfiguracaoConsumidorSubscritor(String idExecutor) {
		super();
		this.codigoIdentificadorUnicoConsumidor = idExecutor;
	}

	public ConfiguracaoConsumidorSubscritor(int idGrupo, String idExecutor, int tempoKeepAliveBalanceamentoSegundos) {
		super();
		this.codigoIdentificadorGrupo = idGrupo;
		this.codigoIdentificadorUnicoConsumidor = idExecutor;
		this.valorCicloTempoAtivo = tempoKeepAliveBalanceamentoSegundos;
	}

	public int getCodigoIdentificadorGrupo() {
		return codigoIdentificadorGrupo;
	}

	public void setCodigoIdentificadorGrupo(int codigoIdentificadorGrupo) {
		this.codigoIdentificadorGrupo = codigoIdentificadorGrupo;
	}

	public int getValorCicloTempoAtivo() {
		return valorCicloTempoAtivo;
	}

	/**
	 * Seta o valor do ciclo de atualização do consumidor em relação ao servidor broker do ima-eventos.
	 * A configuração padrão e mínima é de 5 segundos de ciclo. 
	 * @param valorCicloTempoAtivo
	 */
	public void setValorCicloTempoAtivo(int valorCicloTempoAtivo) {

		if (valorCicloTempoAtivo < 5) {
			this.valorCicloTempoAtivo = 5;
		} else {
			this.valorCicloTempoAtivo = valorCicloTempoAtivo;
		}
	}

	@Override
	public String toString() {
		return "ConfiguracaoConsumidorSubscritor [codigoIdentificadorGrupo=" + codigoIdentificadorGrupo
				+ ", codigoIdentificadorUnicoConsumidor=" + codigoIdentificadorUnicoConsumidor
				+ ", valorCicloTempoAtivo=" + valorCicloTempoAtivo + "]";
	}

	public String getCodigoIdentificadorUnicoConsumidor() {
		return codigoIdentificadorUnicoConsumidor;
	}

	public void setCodigoIdentificadorUnicoConsumidor(String codigoIdentificadorUnicoConsumidor) {
		this.codigoIdentificadorUnicoConsumidor = codigoIdentificadorUnicoConsumidor;
	}

}
