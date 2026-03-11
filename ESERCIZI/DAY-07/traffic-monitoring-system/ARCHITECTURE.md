# Architettura del Sistema

Documentazione dettagliata dell'architettura del sistema di monitoraggio traffico.

## Panoramica

Il sistema implementa un'architettura event-driven basata su Apache Kafka per il monitoraggio del traffico autostradale in tempo reale.

## Diagramma Architetturale

```
┌─────────────────┐
│  TomTom Traffic │
│      API        │
└────────┬────────┘
         │ HTTP/REST
         │ (polling 60s)
         ▼
┌─────────────────┐
│     Producer    │
│   (Java 21)     │
└────────┬────────┘
         │
         │ Kafka Producer API
         ▼
┌─────────────────────────────────────┐
│         Apache Kafka                │
│                                     │
│  Topic: traffic.raw                 │
│  - Partitions: 3                    │
│  - Replication: 1                   │
│  - Retention: 7 days                │
└──────┬──────────────────────────────┘
       │
       │ Kafka Streams API
       ▼
┌─────────────────┐
│  Stream         │
│  Processor      │
│  (Kafka Streams)│
└────┬────────┬───┘
     │        │
     │        │
     ▼        ▼
  Metrics   Alerts
  Topic     Topic
     │        │
     │        │
     ▼        ▼
┌─────────────────┐
│   Consumer      │
│   MongoDB       │
│   (Java 21)     │
└────────┬────────┘
         │
         │ MongoDB Driver
         ▼
┌─────────────────┐
│    MongoDB      │
│                 │
│  - Metrics      │
│  - Alerts       │
└─────────────────┘
```

## Componenti Dettagliati

### 1. Producer TomTom

**Responsabilità:**
- Polling periodico della TomTom Traffic API
- Normalizzazione dati in formato standardizzato
- Pubblicazione su Kafka con partitioning geografico

**Tecnologie:**
- Java 21
- OkHttp per chiamate HTTP
- Jackson per JSON parsing
- Kafka Producer API

**Caratteristiche:**
- **Rate Limiting**: Implementato per rispettare limiti API TomTom
- **Retry Logic**: Backoff esponenziale in caso di errori
- **Partitioning**: Basato su coordinate geografiche per distribuzione bilanciata
- **Serializzazione**: JSON con schema definito

**Configurazioni Kafka Producer:**
```properties
acks=1                    # Attendere ack dal leader
retries=3                 # Retry automatici
enable.idempotence=true   # Garanzia exactly-once
compression.type=snappy   # Compressione messaggi
batch.size=16384          # Batch size per performance
linger.ms=10              # Attesa per batching
```

**Schema Dati Output:**
```json
{
  "latitude": 41.8902,
  "longitude": 12.4922,
  "current_speed": 45.5,
  "free_flow_speed": 90.0,
  "current_travel_time": 120,
  "free_flow_travel_time": 60,
  "confidence": 0.95,
  "road_closure": false,
  "timestamp": 1678901234000
}
```

### 2. Stream Processor

**Responsabilità:**
- Analisi in tempo reale dei dati traffico
- Calcolo metriche aggregate su finestre temporali
- Generazione alert per situazioni critiche

**Tecnologie:**
- Java 21
- Kafka Streams API
- State Stores per aggregazioni

**Topologia Kafka Streams:**

```
Input: traffic.raw
    │
    ├─ Branch 1: Metrics Processing
    │   │
    │   ├─ Group by location
    │   ├─ Window: 5 minutes tumbling
    │   ├─ Aggregate:
    │   │   - Avg current speed
    │   │   - Avg free flow speed
    │   │   - Avg congestion ratio
    │   │   - Max congestion
    │   │   - Total delay
    │   │   - Road closures count
    │   │
    │   └─ Output: traffic.metrics
    │
    └─ Branch 2: Alert Generation
        │
        ├─ Filter: congestion_ratio >= 0.5
        ├─ Map to TrafficAlert:
        │   - Severity: INFO/WARNING/CRITICAL
        │   - Message generation
        │
        └─ Output: traffic.alerts
```

**Alert Severity Levels:**
- **INFO**: 50% < congestion < 75%
- **WARNING**: 75% <= congestion < 90%
- **CRITICAL**: congestion >= 90% OR road_closure = true

**Time Windows:**
- 5 minuti: Metriche real-time
- (Future) 15 minuti: Trend analysis
- (Future) 1 ora: Historical patterns

**Processing Guarantees:**
- **Exactly-once**: Configurato con `processing.guarantee=exactly_once_v2`

### 3. Consumer MongoDB

**Responsabilità:**
- Consumo dati da topic metrics e alerts
- Batch insert su MongoDB per performance
- Gestione indici per query efficienti

**Tecnologie:**
- Java 21
- Kafka Consumer API
- MongoDB Java Driver (Sync)

**Consumer Configuration:**
```properties
auto.offset.reset=earliest
enable.auto.commit=false      # Manual commit
max.poll.records=100          # Batch size
fetch.min.bytes=1024
fetch.max.wait.ms=500
```

**MongoDB Schema:**

**Collection: traffic_metrics**
```javascript
{
  location_key: "4189_1249",      // Lat/Lon rounded
  window_start: 1678901234000,     // Timestamp
  window_end: 1678901534000,
  count: 12,
  avg_current_speed: 45.5,
  avg_free_flow_speed: 85.0,
  avg_congestion_ratio: 0.46,
  max_congestion_ratio: 0.65,
  total_delay: 720,
  road_closures: 0
}
```

**Indexes:**
- `window_start` (descending) - Query temporali
- `location_key` (ascending) - Query geografiche
- `location_key + window_start` (compound) - Query combinate

**Collection: traffic_alerts**
```javascript
{
  severity: "WARNING",
  congestion_ratio: 0.72,
  current_speed: 25.0,
  free_flow_speed: 90.0,
  delay: 180,
  road_closure: false,
  timestamp: 1678901234000,
  message: "WARNING traffic congestion...",
  location: {                      // GeoJSON
    type: "Point",
    coordinates: [12.4922, 41.8902]
  }
}
```

**Indexes:**
- `timestamp` (descending) - Alert recenti
- `severity` (ascending) - Filtro per severity
- `location` (2dsphere) - Query geografiche
- `severity + timestamp` (compound) - Query combinate

## Kafka Topics

### Topic: traffic.raw

**Configurazione:**
```properties
partitions=3
replication.factor=1
retention.ms=604800000    # 7 giorni
compression.type=snappy
cleanup.policy=delete
```

**Key**: `<lat_rounded>_<lon_rounded>` (es. "4189_1249")
**Value**: JSON (TrafficData)
**Throughput stimato**: ~20-50 msg/sec (dipende da polling interval)

### Topic: traffic.metrics

**Configurazione:**
```properties
partitions=3
replication.factor=1
retention.ms=2592000000   # 30 giorni
compression.type=snappy
cleanup.policy=delete
```

**Key**: location_key
**Value**: JSON (TrafficMetrics)
**Throughput stimato**: ~5-15 msg/sec

### Topic: traffic.alerts

**Configurazione:**
```properties
partitions=3
replication.factor=1
retention.ms=2592000000   # 30 giorni
compression.type=snappy
cleanup.policy=delete
```

**Key**: location_key
**Value**: JSON (TrafficAlert)
**Throughput stimato**: Variabile, basato su condizioni traffico

## Flusso Dati End-to-End

### 1. Data Ingestion (Producer)

```
1. Timer trigger (ogni 60s)
2. HTTP GET a TomTom API per area geografica
3. Parse JSON response
4. Per ogni data point:
   a. Crea oggetto TrafficData
   b. Serializza a JSON
   c. Genera partition key (basato su coordinate)
   d. Send a Kafka topic 'traffic.raw'
5. Gestione errori e retry
```

**Latenza**: ~500ms - 2s (dipende da TomTom API)

### 2. Stream Processing

```
1. Poll da topic 'traffic.raw'
2. Branch 1 - Metrics:
   a. Group by location key
   b. Window tumbling 5 min
   c. Aggregate metriche
   d. Emit a 'traffic.metrics' alla fine della window
   
3. Branch 2 - Alerts:
   a. Filter per congestion >= 50%
   b. Map a TrafficAlert
   c. Emit a 'traffic.alerts' immediatamente
```

**Latenza**: ~100-500ms (processing in-memory)

### 3. Persistence (Consumer)

```
1. Poll da 'traffic.metrics' e 'traffic.alerts'
2. Accumula in batch (max 100 records o 1sec)
3. Batch insert a MongoDB
4. Commit offset Kafka
5. Repeat
```

**Latenza**: ~500ms - 2s (batch processing)

### Latenza Totale End-to-End

- **Best case**: ~2-4 secondi (da API call a MongoDB)
- **Average case**: ~5-10 secondi
- **Worst case**: ~30 secondi (con retry e backoff)

## Scalabilità

### Horizontal Scaling

**Producer:**
- Fino a 3 istanze (pari a numero partizioni)
- Ogni istanza scrive su tutte le partizioni
- Load balancing automatico tramite partition key

**Stream Processor:**
- Fino a 3 istanze (pari a numero partizioni input topic)
- Kafka Streams distribuisce automaticamente il processing
- Cada istanza gestisce subset di partizioni

**Consumer:**
- Fino a 3 istanze (pari a numero partizioni)
- Ogni istanza consuma da subset di partizioni
- Consumer group coordination automatica

### Vertical Scaling

**Kafka:**
- Aumentare heap memory (`KAFKA_HEAP_OPTS`)
- Aumentare numero thread I/O
- SSD per log storage

**MongoDB:**
- Aumentare WiredTiger cache
- Sharding per dataset molto grandi (>100GB)

### Limiti Attuali

- **Producer**: TomTom API rate limits (free tier)
- **Kafka**: Single broker (no fault tolerance)
- **MongoDB**: Single instance (no replica set)

## Fault Tolerance

### Producer

- **Failure Handling**: Retry con backoff esponenziale
- **Data Loss**: Configurato `acks=1` (trade-off performance/reliability)
- **Recovery**: Ripartenza automatica da ultimo offset

### Stream Processor

- **State Management**: State store persistenti su disco
- **Failure Recovery**: Kafka Streams riassegna partizioni
- **Exactly-once**: Configurato per evitare duplicati

### Consumer

- **Offset Management**: Manual commit dopo successful insert
- **Failure Recovery**: Ripartenza da ultimo offset committato
- **Data Loss**: Batch insert con rollback in caso di errore

## Security Considerations

### Produzione TODO

1. **Kafka Security:**
   - Enable SASL/SSL
   - ACL per topic access
   - Encryption in transit

2. **MongoDB Security:**
   - Authentication enabled
   - Role-based access control
   - Encryption at rest

3. **Secrets Management:**
   - Kubernetes Secrets per credenziali
   - Rotation periodica API keys
   - Vault integration (opzionale)

## Monitoring e Observability

### Metriche Chiave

**Producer:**
- Throughput (msg/sec)
- Success/Failure rate API calls
- Kafka send latency
- Error rate

**Stream Processor:**
- Processing lag
- State store size
- Throughput per branch
- Window completion time

**Consumer:**
- Consumer lag (offset diff)
- Batch insert latency
- MongoDB write throughput
- Error rate

### Logging

Log levels configurabili via SLF4J:
- INFO: Operazioni principali
- DEBUG: Dettagli processing
- ERROR: Errori e stack traces

### Health Checks

Implementati in Dockerfile e Kubernetes:
- Liveness: Process is running
- Readiness: Service is ready to accept traffic

## Future Improvements

1. **Multi-Region Support**: Deploy in multiple regions
2. **Advanced Analytics**: Machine learning per predizioni
3. **Real-time Dashboard**: Web UI per visualizzazione
4. **Historical Analysis**: Long-term trend analysis
5. **Alert Notifications**: Email/SMS/Webhook per alert critici
6. **API Gateway**: REST API per query dati storici
7. **Data Lake Integration**: Export dati per data science
8. **Kafka Cluster**: Multi-broker setup con replication
9. **MongoDB Replica Set**: High availability
10. **Prometheus/Grafana**: Monitoring stack completo
