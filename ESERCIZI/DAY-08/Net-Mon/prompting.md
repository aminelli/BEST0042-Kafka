# Network Monitoring System - Specifiche Progetto

## 🎯 Obiettivo del Progetto
Realizzare un sistema distribuito di monitoraggio del traffico di rete che raccolga dati di telemetria da dispositivi eterogenei, li normalizzi tramite Apache Spark e li indicizzi su Elasticsearch per analisi e visualizzazione.

## 💼 Competenze Tecniche Richieste
- **Java 21** con features moderne (Records, Pattern Matching, Virtual Threads)
- **Apache Kafka** (Producer API, Consumer API, Kafka Streams)
- **Apache Spark** (Structured Streaming, Dataset API)
- **Elasticsearch** e **Kibana** per storage e visualizzazione
- **Maven** per la gestione delle dipendenze
- **Docker/Docker Compose** per containerizzazione

---

## 📋 Componenti del Sistema

### 1. Producer #1 - Network Device Type A
**Responsabilità:** Simulare telemetria da dispositivi di rete di tipo A (es. Router)

**Specifiche tecniche:**
- Topic Kafka: `network-telemetry-type-a`
- Frequenza invio: 1 messaggio ogni 2-5 secondi (configurabile)
- Formato payload JSON:
```json
{
  "deviceId": "ROUTER-001",
  "timestamp": "2026-03-12T10:30:45.123Z",
  "deviceType": "router",
  "metrics": {
    "bytesIn": 1024567,
    "bytesOut": 892345,
    "packetsIn": 15234,
    "packetsOut": 13421,
    "errorRate": 0.02,
    "interfaces": [
      {"name": "eth0", "status": "UP", "speed": 1000},
      {"name": "eth1", "status": "UP", "speed": 1000}
    ]
  },
  "location": "DataCenter-1"
}
```

**Requisiti:**
- Utilizzo di Kafka Producer con configurazioni ottimizzate (acks, batch size, compression)
- Simulazione realistica con variazione random dei valori
- Gestione errori e retry con backoff esponenziale
- Logging strutturato (SLF4J + Logback)
- Graceful shutdown

---

### 2. Producer #2 - Network Device Type B
**Responsabilità:** Simulare telemetria da dispositivi di rete di tipo B (es. Switch)

**Specifiche tecniche:**
- Topic Kafka: `network-telemetry-type-b`
- Frequenza invio: 1 messaggio ogni 3-7 secondi (configurabile)
- Formato payload JSON (DIVERSO da Type A):
```json
{
  "id": "SWITCH-042",
  "ts": 1710239445123,
  "type": "switch",
  "stats": {
    "totalTraffic": 2048934,
    "activeConnections": 245,
    "droppedPackets": 12,
    "utilization": 67.5,
    "vlanStats": {
      "vlan10": {"traffic": 500000, "devices": 45},
      "vlan20": {"traffic": 350000, "devices": 32}
    }
  },
  "site": "Office-Building-2",
  "firmware": "v2.3.1"
}
```

**Requisiti:**
- Stesse configurazioni di qualità del Producer #1
- Schema payload completamente diverso dal Type A
- Simulazione di condizioni anomale occasionali (spike di traffico, packet loss)

---

### 3. Spark Streaming Application - Data Normalizer
**Responsabilità:** Leggere da entrambi i topic Kafka, normalizzare i dati in un formato unificato e scriverli su un topic di output

**Specifiche tecniche:**

**Input Topics:**
- `network-telemetry-type-a`
- `network-telemetry-type-b`

**Output Topic:**
- `network-telemetry-normalized`

**Schema Normalizzato:**
```json
{
  "normalizedId": "uuid-v4",
  "originalDeviceId": "ROUTER-001 | SWITCH-042",
  "deviceType": "router | switch",
  "timestamp": "2026-03-12T10:30:45.123Z",
  "location": "DataCenter-1 | Office-Building-2",
  "metrics": {
    "totalBytesTransferred": 1916912,
    "totalPackets": 28655,
    "errorRate": 0.02,
    "utilization": 67.5,
    "activeConnections": 245
  },
  "rawData": { /* payload originale */ },
  "processingTimestamp": "2026-03-12T10:30:47.456Z",
  "version": "1.0"
}
```

**Requisiti:**
- Utilizzo di Spark Structured Streaming
- Lettura da Kafka in modalità streaming
- Trasformazione con Dataset API e funzioni custom
- Gestione di entrambi i formati con pattern matching
- Watermarking per gestione eventi tardivi
- Checkpoint per fault tolerance
- Scrittura su Kafka del payload normalizzato
- Metriche di processing (throughput, latency)
- Unit test con dataset di esempio

---

### 4. Elasticsearch Consumer
**Responsabilità:** Consumare i dati normalizzati da Kafka e indicizzarli su Elasticsearch

**Specifiche tecniche:**

**Input Topic:**
- `network-telemetry-normalized`

**Elasticsearch:**
- Indice: `network-telemetry-{YYYY.MM.DD}` (rolling daily)
- Mapping ottimizzato per time-series data
- Retention policy: 30 giorni

**Requisiti:**
- Consumer Kafka con commit manuale
- Bulk indexing su Elasticsearch (batch di 100-500 documenti)
- Gestione backpressure
- Retry logic per fallimenti di indicizzazione
- Dead letter queue per messaggi non processabili
- Health check e monitoring
- Index template pre-configurato

---

## 🏗️ Architettura e Best Practices

### Configurazione Esterna
- Tutti i parametri (broker Kafka, topic, intervalli, batch size) in file di configurazione esterni (YAML/Properties)
- Supporto per variabili d'ambiente per deployment cloud

### Logging e Monitoring
- Logging strutturato JSON per tutti i componenti
- Metriche JMX esposte
- Log levels configurabili
- Tracciamento end-to-end con correlation ID

### Testing
- Unit test per logica di business (JUnit 5)
- Integration test con Testcontainers (Kafka, Elasticsearch)
- Test coverage minimo 80%

### Error Handling
- Gestione eccezioni con retry policy
- Circuit breaker per protezione da fallimenti a cascata
- Dead letter queue per messaggi problematici
- Alerting su anomalie

### Performance
- Compressione Kafka (snappy/lz4)
- Batching per ottimizzare throughput
- Tuning JVM appropriato
- Connection pooling per Elasticsearch

### Deployment
- Dockerfile per ogni componente
- Docker Compose per orchestrazione locale
- Script di setup automatico
- README con istruzioni chiare

---

## 📦 Deliverable Attesi

1. **Codice sorgente** ben strutturato e commentato
2. **File di configurazione** di esempio
3. **Docker Compose** completo per l'intero stack
4. **README.md** con:
   - Architettura del sistema
   - Istruzioni di setup e avvio
   - Esempi di utilizzo
   - Troubleshooting
5. **Test suite** funzionante
6. **Dashboard Kibana** di esempio per visualizzazione dati
7. **Script di inizializzazione** per topic Kafka e index Elasticsearch

---

## 🚀 Criteri di Qualità

- Codice pulito e manutenibile (Clean Code principles)
- Gestione corretta delle risorse (try-with-resources)
- Utilizzo appropriato di pattern asincroni
- Documentazione JavaDoc per classi e metodi pubblici
- Nessun warning del compilatore
- Conformità alle convenzioni Java standard
