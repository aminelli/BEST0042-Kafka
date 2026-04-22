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
```