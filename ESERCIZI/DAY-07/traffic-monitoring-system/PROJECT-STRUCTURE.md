# Struttura del Progetto

```
traffic-monitoring-system/
│
├── producer-tomtom/                    # Producer TomTom Traffic API
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── com/traffic/monitoring/producer/
│   │               ├── TomTomProducerApp.java
│   │               ├── TomTomClient.java
│   │               ├── TrafficData.java
│   │               └── KafkaProducerService.java
│   ├── pom.xml
│   └── Dockerfile
│
├── stream-processor/                   # Kafka Streams Processor
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── com/traffic/monitoring/stream/
│   │               ├── TrafficStreamProcessorApp.java
│   │               ├── TrafficData.java
│   │               ├── TrafficMetrics.java
│   │               ├── TrafficAlert.java
│   │               └── JsonSerde.java
│   ├── pom.xml
│   └── Dockerfile
│
├── consumer-mongodb/                   # Consumer MongoDB
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── com/traffic/monitoring/consumer/
│   │               ├── MongoDbConsumerApp.java
│   │               ├── MongoDbService.java
│   │               ├── TrafficMetricsConsumer.java
│   │               └── TrafficAlertsConsumer.java
│   ├── pom.xml
│   └── Dockerfile
│
├── kubernetes/                         # Kubernetes Manifests
│   ├── 01-namespace.yaml
│   ├── 02-configmap.yaml
│   ├── 03-secret.yaml
│   ├── 04-pvc.yaml
│   ├── 05-zookeeper.yaml
│   ├── 06-kafka.yaml
│   ├── 07-mongodb.yaml
│   ├── 08-producer-tomtom.yaml
│   ├── 09-stream-processor.yaml
│   ├── 10-consumer-mongodb.yaml
│   ├── 11-hpa.yaml
│   └── README.md
│
├── docker-compose.yml                  # Docker Compose per test locale
├── .env.example                        # Template variabili ambiente
├── .gitignore                          # Git ignore
├── README.md                           # Documentazione principale
├── ARCHITECTURE.md                     # Documentazione architettura
├── Makefile                            # Comandi rapidi
├── build-all.sh                        # Script build applicazioni
└── generate-maven-wrapper.sh          # Script generazione Maven Wrapper
```

## File Creati

### Applicazioni Java (3)

1. **producer-tomtom/**
   - ✅ pom.xml con dipendenze Kafka, OkHttp, Jackson
   - ✅ TomTomProducerApp.java - Main application
   - ✅ TomTomClient.java - Client HTTP per TomTom API
   - ✅ TrafficData.java - Modello dati
   - ✅ KafkaProducerService.java - Kafka producer
   - ✅ Dockerfile multi-stage con Java 21

2. **stream-processor/**
   - ✅ pom.xml con dipendenze Kafka Streams
   - ✅ TrafficStreamProcessorApp.java - Main application
   - ✅ TrafficData.java - Modello dati input
   - ✅ TrafficMetrics.java - Metriche aggregate
   - ✅ TrafficAlert.java - Alert generati
   - ✅ JsonSerde.java - Serializzazione JSON custom
   - ✅ Dockerfile multi-stage con Java 21

3. **consumer-mongodb/**
   - ✅ pom.xml con dipendenze Kafka, MongoDB
   - ✅ MongoDbConsumerApp.java - Main application
   - ✅ MongoDbService.java - Servizio MongoDB
   - ✅ TrafficMetricsConsumer.java - Consumer metriche
   - ✅ TrafficAlertsConsumer.java - Consumer alert
   - ✅ Dockerfile multi-stage con Java 21

### Docker e Orchestrazione

4. **Docker Compose**
   - ✅ docker-compose.yml completo con:
     - Zookeeper
     - Kafka
     - MongoDB
     - Kafka UI (monitoring)
     - Producer, Stream Processor, Consumer
   - ✅ .env.example per configurazione

5. **Kubernetes** (11 manifest)
   - ✅ Namespace
   - ✅ ConfigMap
   - ✅ Secret
   - ✅ PersistentVolumeClaims (3)
   - ✅ Zookeeper Deployment + Service
   - ✅ Kafka StatefulSet + Services
   - ✅ MongoDB StatefulSet + Services
   - ✅ Producer Deployment + Service
   - ✅ Stream Processor Deployment + Service
   - ✅ Consumer Deployment + Service
   - ✅ HorizontalPodAutoscaler (3)

### Documentazione

6. **README.md** - Guida completa con:
   - Architettura overview
   - Quick start Docker Compose
   - Deployment Kubernetes
   - Configurazione
   - Monitoring
   - Troubleshooting

7. **ARCHITECTURE.md** - Architettura dettagliata:
   - Diagrammi componenti
   - Flusso dati
   - Schema topic Kafka
   - Schema collezioni MongoDB
   - Scalabilità e fault tolerance

8. **kubernetes/README.md** - Guida Kubernetes:
   - Build e push immagini
   - Deploy passo-passo
   - Monitoring
   - Scaling
   - Troubleshooting

### Utility

9. **Makefile** - Comandi rapidi:
   - make build
   - make up/down
   - make logs
   - make kafka-topics
   - make mongo-shell

10. **build-all.sh** - Script build tutte le applicazioni

11. **generate-maven-wrapper.sh** - Script generazione Maven Wrapper

12. **.gitignore** - Ignore pattern per Git

## Totale File Creati

- **Applicazioni Java**: 12 file sorgente + 3 pom.xml
- **Docker**: 3 Dockerfile + 1 docker-compose.yml
- **Kubernetes**: 11 manifest YAML
- **Documentazione**: 3 README/guide
- **Utility**: 4 script/config
- **TOTALE**: ~37 file

## Prossimi Passi

1. Generare Maven Wrapper:
   ```bash
   ./generate-maven-wrapper.sh
   ```

2. Build applicazioni:
   ```bash
   ./build-all.sh
   ```

3. Configurare API key TomTom:
   ```bash
   cp .env.example .env
   # Editare .env con la propria API key
   ```

4. Testare con Docker Compose:
   ```bash
   make docker-build
   make up
   make logs
   ```

5. Deploy su Kubernetes (opzionale):
   - Seguire kubernetes/README.md
