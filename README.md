# Caipora Broker

Para utilizar clone o projeto e e rode o docker compose da pasta raiz.
```
docker-compose up
```

Para publicar eventos chame o seguinte comando 

```
curl -X POST "http://localhost:8080/caipora/v1/evento" -H  "accept: */*" -H  "Content-Type: application/json" -d "{\"payload\":\"texto que se quer passar como payload \",\"topico\":\"TOPICO_QUE_SE_QUER_PUBLICAR_EVENTO\"}"
```
