package com.ecommerce.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Applicazione principale Flink per il processing degli ordini e-commerce.
 * 
 * Questo job:
 * 1. Legge eventi CDC da Kafka (generati da Debezium)
 * 2. Filtra solo gli eventi di creazione/aggiornamento ordini
 * 3. Calcola aggregazioni in finestre temporali di 1 minuto
 * 4. Scrive i risultati su MongoDB
 * 
 * Pattern utilizzato: Source → Transform → Aggregate → Sink
 */
public class EcommerceStreamProcessor {
    
    private static final Logger LOG = LoggerFactory.getLogger(EcommerceStreamProcessor.class);
    
    // Configurazioni di default
    private static final String DEFAULT_KAFKA_BROKERS = "kafka-1:9092,kafka-2:9092,kafka-3:9092";
    private static final String DEFAULT_KAFKA_TOPIC = "mysql.ecommerce_db.orders";
    private static final String DEFAULT_KAFKA_GROUP_ID = "flink-ecommerce-processor";
    private static final String DEFAULT_MONGO_CONNECTION = "mongodb://admin:adminpassword@mongodb:27017";
    private static final String DEFAULT_MONGO_DATABASE = "ecommerce_analytics";
    private static final String DEFAULT_MONGO_COLLECTION = "order_aggregations";
    private static final int DEFAULT_WINDOW_SIZE_MINUTES = 1;
    
    public static void main(String[] args) throws Exception {
        
        // Parsing parametri di configurazione
        final ParameterTool params = ParameterTool.fromArgs(args);
        
        String kafkaBrokers = params.get("kafka-brokers", DEFAULT_KAFKA_BROKERS);
        String kafkaTopic = params.get("kafka-topic", DEFAULT_KAFKA_TOPIC);
        String kafkaGroupId = params.get("kafka-group-id", DEFAULT_KAFKA_GROUP_ID);
        String mongoConnection = params.get("mongo-connection", DEFAULT_MONGO_CONNECTION);
        String mongoDatabase = params.get("mongo-database", DEFAULT_MONGO_DATABASE);
        String mongoCollection = params.get("mongo-collection", DEFAULT_MONGO_COLLECTION);
        int windowSizeMinutes = params.getInt("window-size-minutes", DEFAULT_WINDOW_SIZE_MINUTES);
        
        LOG.info("=".repeat(80));
        LOG.info("Avvio Flink E-commerce Stream Processor");
        LOG.info("=".repeat(80));
        LOG.info("Configurazione:");
        LOG.info("  Kafka Brokers: {}", kafkaBrokers);
        LOG.info("  Kafka Topic: {}", kafkaTopic);
        LOG.info("  Kafka Group ID: {}", kafkaGroupId);
        LOG.info("  MongoDB Database: {}", mongoDatabase);
        LOG.info("  MongoDB Collection: {}", mongoCollection);
        LOG.info("  Window Size: {} minuti", windowSizeMinutes);
        LOG.info("=".repeat(80));
        
        // Creazione dell'ambiente di esecuzione Flink
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Configurazione checkpoint per fault tolerance
        // I checkpoint vengono salvati ogni 60 secondi
        env.enableCheckpointing(60000);
        
        // Configurazione parallelismo (può essere sovrascritto a livello di operatore)
        env.setParallelism(2);
        
        // Registrazione parametri globali (disponibili ovunque nel job)
        env.getConfig().setGlobalJobParameters(params);
        
        // === 1. CONFIGURAZIONE KAFKA SOURCE ===
        
        LOG.info("Configurazione Kafka Source...");
        
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
            .setBootstrapServers(kafkaBrokers)
            .setTopics(kafkaTopic)
            .setGroupId(kafkaGroupId)
            // Partenza dall'ultimo offset (solo nuovi ordini)
            .setStartingOffsets(OffsetsInitializer.latest())
            // Deserializzazione semplice come stringa JSON
            .setValueOnlyDeserializer(new SimpleStringSchema())
            // Bounded mode = false per stream continuo
            .build();
        
        // === 2. CREAZIONE DELLO STREAM PRINCIPALE ===
        
        LOG.info("Creazione stream da Kafka...");
        
        DataStream<String> kafkaStream = env.fromSource(
            kafkaSource,
            // Strategia watermark con tolleranza di 5 secondi per eventi in ritardo
            WatermarkStrategy
                .<String>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                .withTimestampAssigner((event, timestamp) -> System.currentTimeMillis()),
            "Kafka CDC Source"
        );
        
        // === 3. PARSING E TRASFORMAZIONE EVENTI CDC ===
        
        LOG.info("Configurazione parsing e trasformazione eventi...");
        
        DataStream<DebeziumCdcEvent.OrderData> orderStream = kafkaStream
            // Parsing del JSON Debezium in oggetto Java
            .map(new JsonParser())
            .name("Parse CDC Events")
            
            // Filtro: rimuove eventi null e operazioni di DELETE
            .filter(event -> {
                boolean valid = event != null && 
                                event.getPayload() != null && 
                                event.getPayload().getAfter() != null &&
                                !"d".equals(event.getPayload().getOperation());
                if (!valid) {
                    LOG.info("🔴 Filtrato evento: event={}, payload={}, after={}, op={}", 
                        event != null, 
                        event != null ? event.getPayload() != null : false,
                        event != null && event.getPayload() != null ? event.getPayload().getAfter() != null : false,
                        event != null && event.getPayload() != null ? event.getPayload().getOperation() : "null");
                }
                return valid;
            })
            .name("Filter Valid Orders")
            
            // Estrazione dei dati dell'ordine dall'evento CDC
            .map(event -> {
                LOG.info("✅ Estratto OrderData: orderId={}, customerId={}, totalAmount={}, orderDate={}", 
                    event.getPayload().getAfter().getOrderId(),
                    event.getPayload().getAfter().getCustomerId(),
                    event.getPayload().getAfter().getTotalAmount(),
                    event.getPayload().getAfter().getOrderDate());
                return event.getPayload().getAfter();
            })
            .name("Extract Order Data")
            
            // Filtro aggiuntivo per ordini validi
            .filter(order -> {
                boolean complete = order != null && 
                                  order.getOrderId() != null && 
                                  order.getTotalAmount() != null;
                if (!complete) {
                    LOG.info("🔴 Ordine incompleto: order={}, orderId={}, totalAmount={}", 
                        order != null,
                        order != null ? order.getOrderId() != null : false,
                        order != null ? order.getTotalAmount() != null : false);
                }
                return complete;
            })
            .name("Filter Complete Orders");
        
        // === 4. AGGREGAZIONE IN FINESTRE TEMPORALI ===
        
        LOG.info("Configurazione aggregazione in finestre temporali (processing-time)...");
        
        DataStream<OrderAggregation> aggregatedStream = orderStream
            // Keying dello stream (in questo caso usiamo una chiave dummy per aggregare tutto)
            // In un caso reale si potrebbe fare keying per categoria, città, ecc.
            .keyBy(order -> "all")
            
            // Finestra temporale tumbling di dimensione configurabile (default 1 minuto)
            // Uso processing-time invece di event-time per semplicità
            .window(TumblingProcessingTimeWindows.of(Time.minutes(windowSizeMinutes)))
            
            // Funzione di aggregazione custom
            .aggregate(new OrderAggregateFunction())
            .name("Aggregate Orders");
        
        // === 5. SINK SU MONGODB ===
        
        LOG.info("Configurazione MongoDB Sink...");
        
        aggregatedStream.addSink(
            new MongoDBSink(mongoConnection, mongoDatabase, mongoCollection)
        ).name("MongoDB Sink");
        
        // Logging delle aggregazioni per debug
        aggregatedStream.print();
        
        // === 6. ESECUZIONE DEL JOB ===
        
        LOG.info("Avvio esecuzione job Flink...");
        LOG.info("=".repeat(80));
        
        env.execute("E-commerce Stream Processor");
    }
    
    /**
     * Funzione di aggregazione per calcolare statistiche sugli ordini.
     * Implementa l'interfaccia AggregateFunction di Flink.
     */
    private static class OrderAggregateFunction 
            implements AggregateFunction<DebeziumCdcEvent.OrderData, OrderAggregation, OrderAggregation> {
        
        private static final org.slf4j.Logger LOG = 
            org.slf4j.LoggerFactory.getLogger(OrderAggregateFunction.class);
        
        /**
         * Crea un nuovo accumulatore vuoto all'inizio di ogni finestra.
         */
        @Override
        public OrderAggregation createAccumulator() {
            LOG.info("🔵 Creato nuovo accumulatore per finestra");
            return new OrderAggregation();
        }
        
        /**
         * Aggiunge un nuovo ordine all'accumulatore.
         * Chiamato per ogni elemento della finestra.
         */
        @Override
        public OrderAggregation add(DebeziumCdcEvent.OrderData order, OrderAggregation accumulator) {
            LOG.info("💚 Aggiunto ordine all'aggregazione: orderId={}, totalAmount={}", 
                order.getOrderId(), order.getTotalAmount());
            accumulator.addOrder(order);
            return accumulator;
        }
        
        /**
         * Restituisce il risultato finale dell'aggregazione.
         * Chiamato quando la finestra si chiude.
         */
        @Override
        public OrderAggregation getResult(OrderAggregation accumulator) {
            LOG.info("🎯 Finestra chiusa! Aggregazione finale: totalOrders={}, totalRevenue={}", 
                accumulator.getTotalOrders(), accumulator.getTotalRevenue());
            return accumulator;
        }
        
        /**
         * Merge di due accumulatori.
         * Utilizzato quando Flink combina aggregazioni parziali da task paralleli.
         */
        @Override
        public OrderAggregation merge(OrderAggregation a, OrderAggregation b) {
            LOG.info("🔀 Merge di due accumulatori");
            return a.merge(b);
        }
    }
}
