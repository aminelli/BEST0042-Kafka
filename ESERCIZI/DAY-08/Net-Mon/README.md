# Network Monitoring System

Sistema distribuito di monitoraggio del traffico di rete che raccoglie dati di telemetria da dispositivi eterogenei, li normalizza tramite Apache Spark e li indicizza su Elasticsearch per analisi e visualizzazione.

## 🏗️ Architettura del Sistema

```
┌─────────────┐     ┌─────────────┐
│  Producer A │────▶│   Kafka     │
│  (Router)   │     │  Topic A    │
└─────────────┘     └──────┬──────┘
                           │
┌─────────────┐     ┌──────▼──────┐     ┌──────────────┐     ┌──────────────┐
│  Producer B │────▶│   Kafka     │────▶│    Spark     │────▶│    Kafka     │
│  (Switch)   │     │  Topic B    │     │  Normalizer  │     │ Normalized   │
└─────────────┘     └─────────────┘     └──────────────┘     └──────┬───────┘
                                                                      │
                                                               ┌──────▼───────┐
                                                               │ Elasticsearch│
                                                               │   Consumer   │
                                                               └──────┬───────┘
                                                                      │
                                                               ┌──────▼───────┐
                                                               │Elasticsearch │
                                                               │  + Kibana    │
                                                               └──────────────┘
```

## 📋 Componenti

### 1. Producer Type A (Router Telemetry)
- Simula telemetria da dispositivi router
- Invia dati al topic `network-telemetry-type-a`
- Intervallo: 2-5 secondi
- Metriche: bytes in/out, packets, error rate, interfacce

### 2. Producer Type B (Switch Telemetry)
- Simula telemetria da dispositivi switch
- Invia dati al topic `network-telemetry-type-b`
- Intervallo: 3-7 secondi
- Metriche: traffico totale, connessioni attive, utilization, VLAN stats

### 3. Spark Normalizer
- Legge da entrambi i topic Kafka (Type A e Type B)
- Normalizza i dati in un formato unificato
- Scrive nel topic `network-telemetry-normalized`
- Gestisce checkpoint per fault tolerance

### 4. Elasticsearch Consumer
- Consuma i dati normalizzati da Kafka
- Bulk indexing su Elasticsearch
- Indici rolling giornalieri: `network-telemetry-YYYY.MM.DD`

## 🚀 Quick Start

### Prerequisiti

- Docker e Docker Compose
- Java 21 (per sviluppo locale)
- Maven 3.9+ (per build locale)

### Avvio con Docker Compose

1. **Clone del repository e build del progetto:**
```bash
git clone <repository-url>
cd Net-Mon
mvn clean package
```

2. **Avvio di tutti i servizi:**
```bash
docker-compose up -d
```

3. **Verifica dello stato dei servizi:**
```bash
docker-compose ps
```

4. **Visualizza i log:**
```bash
# Tutti i servizi
docker-compose logs -f

# Servizio specifico
docker-compose logs -f producer-type-a
docker-compose logs -f spark-normalizer
docker-compose logs -f elasticsearch-consumer
```

### Accesso alle UI

- **Kibana:** http://localhost:5601
- **Elasticsearch:** http://localhost:9200

### Configurazione Indice Kibana

1. Apri Kibana: http://localhost:5601
2. Vai a **Stack Management** > **Index Patterns**
3. Crea un nuovo pattern: `network-telemetry-*`
4. Seleziona `timestamp` come campo temporale
5. Vai a **Discover** per visualizzare i dati

## 🔧 Sviluppo Locale

### Build del progetto

```bash
# Build completo
mvn clean install

# Build di un modulo specifico
mvn clean install -pl producer-type-a -am
```

### Setup su Windows

**Prima di eseguire Spark Normalizer su Windows, è necessario configurare winutils.exe:**

```powershell
# PowerShell
.\setup-winutils.ps1
```

oppure

```cmd
# Command Prompt
setup-winutils.bat
```

Questo script scarica automaticamente `winutils.exe` e `hadoop.dll` necessari per l'esecuzione di Spark su Windows, risolvendo l'errore "Could not locate Hadoop executable".

**Per debug in Visual Studio Code:**
- Il progetto include già [`.vscode/launch.json`](.vscode/launch.json) con tutte le configurazioni necessarie
- Per Spark Normalizer, apri la cartella `spark-normalizer` in VS Code e premi `F5`
- Tutti i parametri JVM richiesti (Java 21 modules, Hadoop home) sono già configurati

### Esecuzione locale (senza Docker)

**1. Avvia infrastruttura (Kafka, Elasticsearch, Kibana):**
```bash
docker-compose up -d zookeeper kafka elasticsearch kibana
```

**2. Esegui i componenti Java:**

```bash
# Producer Type A
java -jar producer-type-a/target/router-producer.jar config/producer-type-a.properties

# Producer Type B
java -jar producer-type-b/target/switch-producer.jar config/producer-type-b.properties

# Spark Normalizer (Windows - usa lo script con i parametri JVM corretti)
.\run-spark-normalizer.ps1
# oppure su Linux/Mac
./run-spark-normalizer.sh

# Elasticsearch Consumer
java -jar elasticsearch-consumer/target/elasticsearch-consumer.jar config/elasticsearch-consumer.properties
```

## 📝 Configurazione

I file di configurazione si trovano nella directory `config/`:

- `producer-type-a.properties` - Configurazione producer router
- `producer-type-b.properties` - Configurazione producer switch
- `spark-normalizer.properties` - Configurazione Spark streaming
- `elasticsearch-consumer.properties` - Configurazione consumer Elasticsearch

### Parametri principali

**Producer Type A:**
```properties
kafka.bootstrap.servers=localhost:9092
kafka.topic=network-telemetry-type-a
producer.min.interval.ms=2000
producer.max.interval.ms=5000
device.id=ROUTER-001
device.location=DataCenter-1
```

**Elasticsearch Consumer:**
```properties
kafka.bootstrap.servers=localhost:9092
elasticsearch.url=http://localhost:9200
elasticsearch.username=elastic
elasticsearch.password=N6Qs4E7s
elasticsearch.index.prefix=network-telemetry
consumer.batch.size=100
```

**Nota:** Il consumer supporta Basic Authentication. Se le credenziali (`elasticsearch.username` e `elasticsearch.password`) non sono fornite, la connessione avviene senza autenticazione.

## 🧪 Testing

### Unit Test
```bash
mvn test
```

### Integration Test
```bash
mvn verify
```

### Test manuale con Kafka CLI

**Verifica topic Type A:**
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic network-telemetry-type-a \
  --from-beginning
```

**Verifica topic normalizzato:**
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic network-telemetry-normalized \
  --from-beginning
```

## 📊 Monitoraggio

### Verifica dati in Elasticsearch

```bash
# Conta documenti
curl -X GET "localhost:9200/network-telemetry-*/_count?pretty"

# Ricerca documenti
curl -X GET "localhost:9200/network-telemetry-*/_search?pretty" \
  -H 'Content-Type: application/json' \
  -d '{"query": {"match_all": {}}, "size": 10}'
```

### Log dei componenti

I log sono salvati nella directory `logs/`:
- `router-producer.log`
- `switch-producer.log`
- `spark-normalizer.log`
- `elasticsearch-consumer.log`

## 🛠️ Troubleshooting

### Problema: Kafka non raggiungibile

**Soluzione:**
```bash
docker-compose restart kafka
# Attendi 30 secondi per il bootstrap
```

### Problema: Elasticsearch Consumer non indicizza

**Verifica connessione:**
```bash
curl http://localhost:9200/_cluster/health?pretty
```

**Verifica log consumer:**
```bash
docker-compose logs elasticsearch-consumer
```

### Problema: Spark Normalizer non processa

**Verifica checkpoint:**
```bash
rm -rf checkpoint/
docker-compose restart spark-normalizer
```

## 🗂️ Struttura del Progetto

```
Net-Mon/
├── common/                      # Modelli e utility condivisi
│   └── src/main/java/com/netmon/common/
│       ├── model/              # Record Java per i dati
│       └── util/               # Utility JSON
├── producer-type-a/            # Producer Router
│   ├── src/main/java/com/netmon/producer/
│   └── Dockerfile
├── producer-type-b/            # Producer Switch
│   ├── src/main/java/com/netmon/producer/
│   └── Dockerfile
├── spark-normalizer/           # Spark Streaming Normalizer
│   ├── src/main/java/com/netmon/spark/
│   └── Dockerfile
├── elasticsearch-consumer/     # Elasticsearch Consumer
│   ├── src/main/java/com/netmon/consumer/
│   └── Dockerfile
├── config/                     # File di configurazione
│   ├── producer-type-a.properties
│   ├── producer-type-b.properties
│   ├── spark-normalizer.properties
│   └── elasticsearch-consumer.properties
├── docker-compose.yml          # Orchestrazione Docker
├── pom.xml                     # POM parent Maven
└── README.md
```

## 🎯 Funzionalità Implementate

✅ Producer Kafka con compressione e idempotenza  
✅ Simulazione realistica di telemetria con variazioni random  
✅ Gestione errori con retry e backoff esponenziale  
✅ Logging strutturato con Logback  
✅ Graceful shutdown per tutti i componenti  
✅ Spark Structured Streaming con checkpoint  
✅ Normalizzazione di payload eterogenei  
✅ Bulk indexing su Elasticsearch  
✅ Indici rolling giornalieri  
✅ Docker Compose per orchestrazione completa  
✅ Configurazione esternalizzata  

## 📚 Tecnologie Utilizzate

- **Java 21** - Records, Pattern Matching, Virtual Threads
- **Apache Kafka 3.6.1** - Message broker
- **Apache Spark 3.5.0** - Stream processing
- **Elasticsearch 8.12.0** - Search e analytics
- **Kibana 8.12.0** - Visualizzazione dati
- **Maven** - Build automation
- **Docker & Docker Compose** - Containerizzazione

## 📄 Licenza

Questo progetto è fornito come esempio educativo.

## 👥 Autori

Sviluppato seguendo le specifiche del documento `prompting.md`.
