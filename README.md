# Caipora Broker

Para utilizar clone o projeto e e rode o docker compose da pasta raiz.
```
docker-compose up
```

Para publicar eventos chame o seguinte comando 

```
curl -X POST "http://localhost:8080/caipora/v1/evento" -H  "accept: */*" -H  "Content-Type: application/json" -d "{\"payload\":\"texto que se quer passar como payload \",\"topico\":\"TOPICO_NO_QUAL_SE_QUER_PUBLICAR_EVENTO\"}"
```

Para consumir eventos chame o seguinte comando :

```
while clear;do curl -X POST "http://localhost:8080/caipora/v1/proximo" -H  "accept: */*" -H  "Content-Type: application/json" -d '{"idExecutor":"IdentificadorUnicoConsumidor","idGrupo":"TOPICO_DO_QUAL_SE_QUER_CONSUMIR_EVENTOS"}'|jq; sleep 1; done
```
O grupo de consumo pode ser um filtro do topico :
Os topico SENSOR_TEMPERATURA e SENSOR_ENERGIA podem ter seus eventos consumidos inteiramente pelo consumidor de grupo SENSOR_* por exemplo.
