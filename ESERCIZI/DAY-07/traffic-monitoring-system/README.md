# Sistema di Monitoraggio Traffico Autostradale

Sistema completo di monitoraggio del traffico autostradale in tempo reale basato su Apache Kafka, con raccolta dati da TomTom Traffic API, analisi stream e persistenza su MongoDB.

## 📋 Indice

- [Architettura](#architettura)
- [Stack Tecnologico](#stack-tecnologico)
- [Componenti](#componenti)
- [Prerequisiti](#prerequisiti)
- [Quick Start - Docker Compose](#quick-start---docker-compose)
- [Deployment Kubernetes](#deployment-kubernetes)
- [Build Applicazioni](#build-applicazioni)
- [Configurazione](#configurazione)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)

## 🏗️ Architettura

```
TomTom API → Producer → Kafka (topic: traffic.raw)
                          ↓
                    Stream Processor
                     ↓         ↓
           (traffic.metrics) (traffic.alerts)
                     ↓         ↓
                  MongoDB Consumer
                          ↓
                     MongoDB
```

### Flusso Dati

1. **Producer TomTom**: Raccoglie dati dal TomTom Traffic API ogni 60 secondi e li pubblica sul topic `traffic.raw`
2. **Stream Processor**: Analizza i dati in tempo reale con finestre temporali di 5 minuti, genera metriche aggregate e alert
3. **Consumer MongoDB**: Persiste metriche e alert su MongoDB per analisi storiche

## 🛠️ Stack Tecnologico

- **Java 21** - Linguaggio di programmazione
- **Apache Maven** - Build automation
- **Apache Kafka 3.6.1** - Piattaforma di streaming
- **Kafka Streams** - Stream processing
- **MongoDB 7.0** - Database NoSQL
- **Docker & Docker Compose** - Containerizzazione
- **Kubernetes** - Orchestrazione produzione

## 📦 Componenti

### 1. Producer TomTom (`producer-tomtom/`)

Applicazione Java che:
- Si connette alla TomTom Traffic API
- Effettua polling periodico dei dati sul traffico
- Normalizza i dati in formato JSON
- Pubblica su Kafka topic `traffic.raw`
- Gestisce retry e rate limiting

### 2. Stream Processor (`stream-processor/`)

Applicazione Kafka Streams che:
- Analizza dati traffico in tempo reale
- Calcola metriche aggregate (velocità media, congestione, ritardi)
- Utilizza finestre temporali di 5 minuti
- Genera alert per traffico intenso (soglia >50% congestione)
- Pubblica su topic `traffic.metrics` e `traffic.alerts`

### 3. Consumer MongoDB (`consumer-mongodb/`)

Applicazione Java che:
- Consuma dati da topic metrics e alerts
- Batch insert su MongoDB per performance
- Crea indici per query efficienti
- Gestisce due collezioni: `traffic_metrics` e `traffic_alerts`

## ✅ Prerequisiti

### Per Docker Compose

- Docker Engine 20.10+
- Docker Compose 2.0+
- TomTom API Key (gratuita): https://developer.tomtom.com/

### Per Kubernetes

- Kubernetes cluster (minikube, k3s, EKS, GKE, AKS)
- kubectl configurato
- Helm (opzionale)
- Registry Docker per immagini custom

## 🚀 Quick Start - Docker Compose

### 1. Clone e Configurazione

```bash
cd traffic-monitoring-system

# Copia il file .env di esempio
cp .env.example .env

# Modifica .env con la tua API key TomTom
nano .env
```

### 2. Build Applicazioni

```bash
# Build del producer
cd producer-tomtom
./mvnw clean package
cd ..

# Build dello stream processor
cd stream-processor
./mvnw clean package
cd ..

# Build del consumer
cd consumer-mongodb
./mvnw clean package
cd ..
```

### 3. Avvio con Docker Compose

```bash
# Build delle immagini Docker
docker-compose build

# Avvio di tutti i servizi
docker-compose up -d

# Visualizza i log
docker-compose logs -f

# Verifica lo stato dei servizi
docker-compose ps
```

### 4. Verifica Funzionamento

```bash
# Kafka UI (opzionale)
# Apri browser: http://localhost:8080

# Verifica topic Kafka
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# Verifica consumer group
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 --list

# Connettiti a MongoDB
docker exec -it mongodb mongosh

# In mongosh:
use traffic_monitoring
db.traffic_metrics.count()
db.traffic_alerts.find().limit(5)
```

### 5. Stop Sistema

```bash
docker-compose down

# Con rimozione volumi (attenzione: cancella tutti i dati)
docker-compose down -v
```

## ☸️ Deployment Kubernetes

### 1. Preparazione Immagini

```bash
# Build e tag delle immagini
docker build -t your-registry/producer-tomtom:1.0.0 ./producer-tomtom
docker build -t your-registry/stream-processor:1.0.0 ./stream-processor
docker build -t your-registry/consumer-mongodb:1.0.0 ./consumer-mongodb

# Push al registry
docker push your-registry/producer-tomtom:1.0.0
docker push your-registry/stream-processor:1.0.0
docker push your-registry/consumer-mongodb:1.0.0
```

### 2. Configurazione Secret

```bash
# Crea Secret con API key TomTom
echo -n 'YOUR_TOMTOM_API_KEY' | base64

# Modifica kubernetes/03-secret.yaml con il valore base64
```

### 3. Deploy su Kubernetes

```bash
# Apply di tutti i manifest
kubectl apply -f kubernetes/

# Verifica deployment
kubectl get all -n traffic-monitoring

# Verifica pod
kubectl get pods -n traffic-monitoring -w

# Verifica logs
kubectl logs -n traffic-monitoring deployment/producer-tomtom -f
kubectl logs -n traffic-monitoring deployment/stream-processor -f
kubectl logs -n traffic-monitoring deployment/consumer-mongodb -f
```

### 4. Monitoraggio

```bash
# Describe deployment
kubectl describe deployment producer-tomtom -n traffic-monitoring

# Porta forward MongoDB (debugging)
kubectl port-forward -n traffic-monitoring svc/mongodb 27017:27017

# Porta forward Kafka (debugging)
kubectl port-forward -n traffic-monitoring svc/kafka 9092:9092

# HPA status
kubectl get hpa -n traffic-monitoring
```

### 5. Scaling Manuale

```bash
# Scale producer
kubectl scale deployment producer-tomtom --replicas=2 -n traffic-monitoring

# Scale stream processor
kubectl scale deployment stream-processor --replicas=3 -n traffic-monitoring
```

### 6. Cleanup

```bash
# Rimuovi tutte le risorse
kubectl delete namespace traffic-monitoring
```

## ⚙️ Configurazione

### Variabili d'Ambiente - Producer

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| `TOMTOM_API_KEY` | - | API key TomTom (richiesta) |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Server Kafka |
| `KAFKA_TOPIC_TRAFFIC_RAW` | `traffic.raw` | Topic output |
| `POLLING_INTERVAL_SECONDS` | `60` | Intervallo polling |
| `MIN_LAT` | `41.8` | Latitudine minima area |
| `MIN_LON` | `12.4` | Longitudine minima area |
| `MAX_LAT` | `42.0` | Latitudine massima area |
| `MAX_LON` | `12.6` | Longitudine massima area |

### Variabili d'Ambiente - Stream Processor

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Server Kafka |
| `APPLICATION_ID` | `traffic-stream-processor` | Application ID |

### Variabili d'Ambiente - Consumer

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Server Kafka |
| `MONGODB_URI` | `mongodb://localhost:27017` | URI MongoDB |
| `MONGODB_DATABASE` | `traffic_monitoring` | Database name |
| `CONSUMER_GROUP_ID` | `mongodb-consumer-group` | Consumer group ID |

## 📊 Monitoring

### Kafka Topics

```bash
# Lista topic
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Describe topic
docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
  --describe --topic traffic.raw

# Consuma messaggi (test)
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic traffic.alerts \
  --from-beginning
```

### MongoDB Queries

```javascript
// Connetti a MongoDB
use traffic_monitoring

// Count alert per severity
db.traffic_alerts.aggregate([
  { $group: { _id: "$severity", count: { $sum: 1 } } }
])

// Alert recenti (ultimi 10)
db.traffic_alerts.find().sort({ timestamp: -1 }).limit(10)

// Metriche con maggior congestione
db.traffic_metrics.find().sort({ avg_congestion_ratio: -1 }).limit(10)

// Query geografica (alert in area specifica)
db.traffic_alerts.find({
  location: {
    $near: {
      $geometry: { type: "Point", coordinates: [12.5, 41.9] },
      $maxDistance: 5000  // 5 km
    }
  }
})
```

## 🔧 Troubleshooting

### Producer non parte

```bash
# Verifica configurazione
docker logs producer-tomtom

# Verifica connessione Kafka
docker exec producer-tomtom nc -zv kafka 9092

# Verifica API key TomTom
echo $TOMTOM_API_KEY
```

### Stream Processor in errore

```bash
# Verifica logs
docker logs stream-processor

# Verifica topic input esistente
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Reset applicazione (cancella state)
docker-compose down
docker volume rm traffic-monitoring-system_stream-state
docker-compose up -d
```

### Consumer non scrive su MongoDB

```bash
# Verifica connessione MongoDB
docker exec consumer-mongodb nc -zv mongodb 27017

# Verifica topic esistenti
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Verifica consumer group
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe --group mongodb-consumer-group-metrics
```

### Kafka out of memory

```bash
# Aumenta memoria in docker-compose.yml
environment:
  KAFKA_HEAP_OPTS: "-Xms2G -Xmx4G"
```

## 📝 Note

- **API Rate Limiting**: TomTom free tier ha limiti. Monitorare utilizzo.
- **Retention Kafka**: Configurato per 7 giorni. Modificare se necessario.
- **MongoDB Indexes**: Creati automaticamente al primo avvio consumer.
- **Scaling**: Stream processor richiede unique application.id per istanza.

## 📚 Risorse

- [TomTom Traffic API Documentation](https://developer.tomtom.com/traffic-api/documentation)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka Streams Documentation](https://kafka.apache.org/documentation/streams/)
- [MongoDB Java Driver](https://www.mongodb.com/docs/drivers/java/sync/current/)

## 📄 Licenza

Questo progetto è fornito come esempio didattico.
