# Documentazione per esecuzione passo passo


```sh

# COMPILAZIONE PROGETTO JAVA
# Lanciare script
step-001-java-compile.bat

# AVVIO AMBIENTE CONTENIERIZZATO

docker compose -p kafka-flink up -d mysql
docker compose -p kafka-flink up -d mongodb

docker compose -p kafka-flink up -d kafka-1
docker compose -p kafka-flink up -d kafka-2
docker compose -p kafka-flink up -d kafka-3

docker compose -p kafka-flink up -d kafka-ui

docker compose -p kafka-flink up -d flink-jobmanager
docker compose -p kafka-flink up -d flink-taskmanager

docker compose -p kafka-flink up -d kafka-connect

# Tools jq per dialogare con servizio api di kafka connect
winget install jqlang.jq

# Registrazione del connettore a mysql
curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d @kafka-connect/connectors/mysql-source.json

# Verifica della registrazione del connettore
curl -s http://localhost:8083/connectors | 

# Verifica dello stato del connettore
curl -s http://localhost:8083/connectors/mysql-source-connector/status | jq

# Modifica permessi cartella checkpoints del jobmanager di flink
docker exec ecommerce-flink-jobmanager chown -R flink:flink /opt/flink/checkpoints

# copia del jar nel flink job manager
docker cp ./flink-job/target/flink-ecommerce-processor-1.0.0.jar ecommerce-flink-jobmanager:/tmp/

# Avvio del job 
docker exec ecommerce-flink-jobmanager flink run -d /tmp/flink-ecommerce-processor-1.0.0.jar

# Avvio generatore dati
docker compose -p kafka-flink up -d data-generator

```