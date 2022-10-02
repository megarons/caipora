package br.com.caipora.eventos.v1.rest;
//package br.com.caipora.eventos.v1.rest;
//
//import static io.restassured.RestAssured.given;
//
//import org.junit.jupiter.api.Test;
//
//import br.com.caipora.eventos.v1.models.ConfiguracaoConsumidorSubscrito;
//import io.quarkus.test.junit.QuarkusTest;
//import io.restassured.http.ContentType;
//
//@QuarkusTest
//public class IMAEventosResourceTest {
//
//	@Test
//    public void testConsumidorNaoPassouConfiguracoes() {
//		
//		ConfiguracaoConsumidorSubscrito consumidor = new ConfiguracaoConsumidorSubscrito();
//		
//		consumidor.setIdExecutor("id_executor");
//		consumidor.setIdGrupo(1);
//		consumidor.setIdentificadorEntrega(1);
//		consumidor.setEstadoDocumento(1);
//		consumidor.setTipologiaDocumento(1);
//		
//		given()
//		.body(consumidor)
//		.contentType(ContentType.JSON)
//		.when().post("/ima/v1/evento/proximo")
//		.then()
//		.assertThat()
//		.statusCode(500);
//    }
//}