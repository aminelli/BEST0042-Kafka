# Sistema di Monitoraggio Traffico Autostradale con Apache Kafka

## Il tuo ruolo
Sei un esperto in:
- Analisi del traffico autostradale e gestione dei dati di mobilità
- Architetture event-driven basate su Apache Kafka
- Progettazione e implementazione di pipeline di streaming in tempo reale
- Sistemi IoT e telemetria per il monitoraggio del traffico

## Obiettivo del progetto
Progettare e realizzare un sistema completo di monitoraggio del traffico autostradale in tempo reale basato su Apache Kafka, con capacità di rilevamento automatico delle criticità e generazione di alert.

**Si richiede lo sviluppo completo delle applicazioni Java necessarie** per implementare tutti i componenti del sistema (producer, consumer, stream processor) utilizzando esclusivamente lo stack tecnologico specificato.

## Stack Tecnologico
Il sistema deve essere implementato utilizzando:
- **Java 21** - Linguaggio di programmazione per tutti i componenti (producer, consumer, stream processor)
- **Apache Maven** - Build automation e dependency management
- **Apache Kafka** - Piattaforma di streaming distribuita
- **Kafka Streams API** - Framework per lo stream processing (integrato con Kafka)
- **MongoDB** - Database NoSQL per la persistenza dei dati elaborati dagli stream
- **Docker** - Containerizzazione delle applicazioni
- **Docker Compose** - Orchestrazione ambiente di test locale
- **Kubernetes** - Orchestrazione e deployment in produzione

Tutti gli esempi di codice, configurazioni Maven (pom.xml) e implementazioni devono utilizzare esclusivamente queste tecnologie.

## Requisiti specifici

### 1. Requisiti Funzionali
Fornisci un'analisi dettagliata dei requisiti funzionali necessari per:
- Raccolta dati in tempo reale sul traffico autostradale
- Elaborazione e normalizzazione dei dati provenienti da fonti eterogenee
- Tracciamento dello stato del traffico per segmenti stradali
- Storicizzazione dei dati per analisi predittive

### 2. Sistema di Alert
Progetta un sistema di alert che:
- Rilevi automaticamente situazioni di traffico intenso o strade bloccate
- Definisca soglie dinamiche basate su parametri configurabili (velocità media, densità veicolare, tempo di percorrenza)
- Classifichi gli alert per severità (info, warning, critical)
- Preveda meccanismi di notifica multipli (topic Kafka dedicati, webhook, etc.)

### 3. Sorgenti Dati - TomTom Traffic API
Il sistema deve utilizzare **TomTom Traffic API** come servizio per il recupero dei dati sul traffico in tempo reale.

Analizza e implementa:
- Registrazione e configurazione dell'API key TomTom
- Endpoint API da utilizzare (Traffic Flow, Traffic Incidents, etc.)
- Struttura dei dati restituiti dall'API TomTom
- Frequenza di aggiornamento e limiti delle chiamate API
- Copertura geografica e aree di interesse da monitorare
- Gestione della quota gratuita e ottimizzazione delle richieste
- Parsing e mappatura dei dati TomTom verso il formato interno del sistema

### 4. Producer Kafka
Progetta e implementa un producer Java che:
- Si connetta alla **TomTom Traffic API** utilizzando le librerie HTTP appropriate (es. Apache HttpClient, OkHttp)
- Gestisca l'autenticazione tramite API key
- Effettui il polling dei dati con rate limiting appropriato rispettando i limiti TomTom
- Normalizzi i dati TomTom in un formato comune (schema Avro/JSON suggerito)
- Gestisca errori HTTP e retry con backoff esponenziale
- Pubblichi i dati su topic Kafka dedicati con partitioning strategico
- Implementi logging e metriche per monitorare le chiamate API

### 5. Stream Processing
Implementa uno stream processor Kafka (Kafka Streams o ksqlDB) che:
- Analizzi i dati in tempo reale per identificare aree a maggior traffico
- Calcoli metriche aggregate (velocità media, densità, trend)
- Utilizzi finestre temporali per analisi su diversi intervalli (5 min, 15 min, 1 ora)
- Generi eventi di alert quando vengono superate le soglie definite
- Arricchisca i dati con informazioni contestuali (storico, meteo, eventi)

### 6. Persistenza Dati con MongoDB
Progetta un consumer Kafka che:
- Legga i dati elaborati dai topic Kafka Stream
- Salvi i risultati delle analisi e degli alert in MongoDB
- Utilizzi collezioni appropriate per diversi tipi di dati (metriche aggregate, alert, dati storici)
- Implementi batch insert per ottimizzare le performance
- Gestisca indici MongoDB per query efficienti su timestamp, area geografica, severità alert
- Preveda una strategia di data retention e archiviazione

### 7. Containerizzazione e Deployment

#### 7.1 Dockerfile per Applicazioni Java
Crea Dockerfile ottimizzati per ciascuna applicazione Java:
- **Dockerfile per Producer** - Immagine custom per l'applicazione producer TomTom
- **Dockerfile per Stream Processor** - Immagine custom per l'applicazione Kafka Streams
- **Dockerfile per Consumer MongoDB** - Immagine custom per l'applicazione consumer

Caratteristiche richieste per i Dockerfile:
- Utilizzo di immagini base Java 21 (es. eclipse-temurin:21-jre-alpine)
- Multi-stage build per ottimizzare le dimensioni delle immagini
- Copia degli artifact Maven (JAR)
- Configurazione di variabili d'ambiente per parametri runtime
- Definizione di health check appropriati
- Esecuzione con utente non-root per sicurezza

#### 7.2 Docker Compose per Ambiente di Test
Crea un file `docker-compose.yml` completo per l'ambiente di test locale che includa:
- **Zookeeper** - Per la gestione del cluster Kafka
- **Apache Kafka** - Broker Kafka (configurato per sviluppo)
- **MongoDB** - Database per la persistenza
- **Producer Application** - Servizio producer TomTom (immagine custom)
- **Stream Processor Application** - Servizio Kafka Streams (immagine custom)
- **Consumer Application** - Servizio consumer MongoDB (immagine custom)
- **Kafka UI** (opzionale) - Tool per monitoraggio topic e messaggi (es. kafka-ui, akhq)

Caratteristiche richieste:
- Network dedicato per i servizi
- Volume per persistenza dati Kafka e MongoDB
- Variabili d'ambiente configurabili tramite file .env
- Depends_on e health checks per gestire l'ordine di startup
- Porte esposte per accesso ai servizi

#### 7.3 Deployment su Kubernetes
Crea un piano di deployment completo per Kubernetes che includa:

**Manifest Kubernetes richiesti:**
- **Namespace** - Namespace dedicato per l'applicazione
- **ConfigMaps** - Configurazioni delle applicazioni (Kafka bootstrap servers, MongoDB connection, etc.)
- **Secrets** - Credenziali sensibili (TomTom API key, MongoDB credentials)
- **Deployments** - Per producer, stream processor e consumer con replica sets
- **Services** - Esposizione dei servizi (ClusterIP, LoadBalancer se necessario)
- **StatefulSets** - Per Kafka e MongoDB (se non usati come servizi esterni)
- **PersistentVolumeClaims** - Per storage persistente di Kafka e MongoDB
- **HorizontalPodAutoscaler** (opzionale) - Autoscaling basato su CPU/memoria
- **NetworkPolicies** (opzionale) - Politiche di rete per sicurezza

**Strategie di deployment:**
- Rolling update per zero-downtime deployment
- Resource limits e requests appropriati per ciascun pod
- Liveness e readiness probes per health checking
- Considerazioni su affinity/anti-affinity per distribuzione dei pod

**Organizzazione file Kubernetes:**
- Struttura a directory per ambiente (dev, staging, prod)
- Uso di Kustomize o Helm per template parametrizzati (opzionale ma consigliato)

### 8. Architettura e Best Practices
Specifica:
- Topologia dei topic Kafka (naming convention, numero partizioni, replication factor)
- Schema di serializzazione dati consigliato
- Strategie di partitioning per ottimizzare il throughput
- Configurazioni Kafka critiche per il caso d'uso
- Schema MongoDB (collezioni, documenti, indici)
- Considerazioni su scalabilità e fault tolerance

## Deliverable attesi
Per ciascun requisito, fornisci:
- Descrizione dettagliata della soluzione proposta
- Esempi di codice/configurazione dove applicabile
- Diagrammi architetturali (descritti in formato testuale o mermaid)
- Considerazioni su performance, scalabilità e manutenibilità

**File di deployment e containerizzazione:**
- Dockerfile per ciascuna applicazione Java (producer, stream processor, consumer)
- File docker-compose.yml completo per ambiente di test
- Manifest Kubernetes completi (yaml) per deployment in produzione
- File README con istruzioni per build, test locale e deployment su Kubernetes


