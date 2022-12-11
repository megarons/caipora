package br.com.caipora.eventos.v1.dao;

import io.quarkus.runtime.configuration.ProfileManager;

public class DB2_H2_Utils {

	public static String timestampSuperaTempoDeterminado(String variavel, int tempo) {
		String activeProfile = ProfileManager.getActiveProfile();
		if(activeProfile.equals("test")) {
			return  " TIMESTAMPDIFF( SECOND , current_timestamp , "+variavel+" ) > "+tempo+" ";
		}else {
		    return " "+variavel+" < CURRENT_TIMESTAMP  - interval '"+tempo+" seconds' ";
		    		
		}
	}
	
	public static String timestampEstaContidoNoTempoDeterminado(String variavel, int tempo) {
		String activeProfile = ProfileManager.getActiveProfile();
		if(activeProfile.equals("test")) {
			return  " TIMESTAMPDIFF( SECOND , current_timestamp , "+variavel+" ) > "+tempo+" ";
		}else {
		    return " "+variavel+" > CURRENT_TIMESTAMP  - interval '"+tempo+" seconds' ";
		    		
		}
	}

}
