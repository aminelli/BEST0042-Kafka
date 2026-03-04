# Docker Kafka Test

## Step 1 - Creazione Rete

```shell
docker network create net-kafka
```

## Step 2 - Creazione container con istanza Kafka (standalone)

```shell
# Creazione container
# docker run -d --hostname kafka01 --name kafka01 -p 9092:9092 --network net-kafka apache/kafka:3.9.1
docker run -d --hostname kafka01 --name kafka01 -p 9092:9092 --network net-kafka apache/kafka:4.2.0

# Verifichiamo i log del container
docker logs -f kafka01
```

## Step 3 - Creazione Topic

```shell
# Aggangio al container di kafka; viene avviato un nuovo pid che attiva la bash
docker exec -it kafka01 /bin/bash

# mi sposto nella cartella del container dove risiedono i tools a linea di comando di kafka
cd opt/kafka/bin/

# Creazione Topic
./kafka-topics.sh --create --topic test-corso --bootstrap-server localhost:9092

# Verifica Creazione Topic
./kafka-topics.sh --describe --topic test-corso --bootstrap-server localhost:9092
```

## Step 4 - Creazione Producer

Apriamo un nuovo terminale a livello di os e lanciamo il seguente comando:
```shell
docker exec -it kafka01 /opt/kafka/bin/kafka-console-producer.sh --topic test-corso --bootstrap-server localhost:9092
```

## Step 5 - Creazione Consumer

Apriamo un nuovo terminale a livello di os e lanciamo il seguente comando:
```shell
docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic test-corso --from-beginning --bootstrap-server localhost:9092
``` 


## Step 6 - Creazione di un kafka connect per monitorare un file di testo

# connect-file-3.9.1.jar
connect-file-4.2.0.jar

```shell
# Aggangio al container di kafka; viene avviato un nuovo pid che attiva la bash
docker exec -it kafka01 /bin/bash

# mi sposto nella cartella dove monitorare il file di testo
cd opt/kafka/

# Aggiungo la configurazione del plugin alla fine del file di properties relativo
# echo "plugin.path=libs/connect-file-3.9.1.jar" >> config/connect-standalone.properties
echo "plugin.path=libs/connect-file-4.2.0.jar" >> config/connect-standalone.properties
echo -e "message01 \nmessage02 \nmessage03 \nmessage04 \nmessage04" > test.txt
```

## Step 7 - Creazione Kafka Connect

Apriamo un nuovo terminale a livello di os e lanciamo il seguente comando:
```shell

# Aggangio al container di kafka; viene avviato un nuovo pid che attiva la bash
docker exec -it kafka01 /bin/bash

# mi sposto nella cartella dove monitorare il file di testo
cd opt/kafka/

bin/connect-standalone.sh config/connect-standalone.properties config/connect-file-source.properties config/connect-file-sink.properties
```

## Step 8 - Creazione Consumer che scoda su topic dedicato alla lettura del file

Apriamo un nuovo terminale a livello di os e lanciamo il seguente comando:
```shell
docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic connect-test --from-beginning --bootstrap-server localhost:9092
```

Nota: Per monitorar il file test.txt e test.sink.txt (in separati command)

```shell
#  Monitoring file sorgente
docker exec -it kafka01 tail -f /opt/kafka/test.txt
```

```shell
#  Monitoring file di sink (destinazione)
docker exec -it kafka01 tail -f /opt/kafka/test.sink.txt
```

