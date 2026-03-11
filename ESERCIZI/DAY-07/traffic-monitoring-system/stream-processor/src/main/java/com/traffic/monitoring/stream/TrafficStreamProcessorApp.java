package com.traffic.monitoring.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

/**
 * Kafka Streams application for traffic data processing and alerting
 */
public class TrafficStreamProcessorApp {
    private static final Logger logger = LoggerFactory.getLogger(TrafficStreamProcessorApp.class);
    
    // Topic names
    private static final String INPUT_TOPIC = "traffic.raw";
    private static final String METRICS_TOPIC = "traffic.metrics";
    private static final String ALERTS_TOPIC = "traffic.alerts";
    
    // Alert thresholds
    private static final double CONGESTION_THRESHOLD = 0.5; // 50% congestion
    private static final double CRITICAL_CONGESTION_THRESHOLD = 0.75; // 75% congestion
    
    public static void main(String[] args) {
        logger.info("Starting Traffic Stream Processor...");
        
        // Load configuration
        String kafkaBootstrapServers = getEnvOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String applicationId = getEnvOrDefault("APPLICATION_ID", "traffic-stream-processor");
        
        Properties props = createStreamsConfig(kafkaBootstrapServers, applicationId);
        
        // Build topology
        StreamsBuilder builder = new StreamsBuilder();
        buildTopology(builder);
        
        // Create and start streams
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Traffic Stream Processor...");
            streams.close(Duration.ofSeconds(10));
            logger.info("Traffic Stream Processor stopped.");
        }));
        
        // Start processing
        streams.start();
        logger.info("Traffic Stream Processor is running.");
    }
    
    private static Properties createStreamsConfig(String bootstrapServers, String applicationId) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        
        // Processing guarantees
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        
        // State store configuration
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
        
        // Optimization settings
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);
        
        return props;
    }
    
    private static void buildTopology(StreamsBuilder builder) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        
        // Create JSON Serdes
        JsonSerde<TrafficData> trafficDataSerde = new JsonSerde<>(TrafficData.class, objectMapper);
        JsonSerde<TrafficMetrics> metricsSerde = new JsonSerde<>(TrafficMetrics.class, objectMapper);
        JsonSerde<TrafficAlert> alertSerde = new JsonSerde<>(TrafficAlert.class, objectMapper);
        
        // Input stream
        KStream<String, String> rawTrafficStream = builder.stream(INPUT_TOPIC);
        
        // Parse JSON and filter invalid records
        KStream<String, TrafficData> trafficStream = rawTrafficStream
                .mapValues(value -> {
                    try {
                        return objectMapper.readValue(value, TrafficData.class);
                    } catch (Exception e) {
                        logger.error("Error parsing traffic data", e);
                        return null;
                    }
                })
                .filter((key, value) -> value != null);
        
        // Branch 1: Calculate metrics with windowing
        processMetrics(trafficStream, metricsSerde);
        
        // Branch 2: Generate alerts
        processAlerts(trafficStream, alertSerde, objectMapper);
    }
    
    private static void processMetrics(KStream<String, TrafficData> trafficStream, 
                                       JsonSerde<TrafficMetrics> metricsSerde) {
        // Aggregate traffic metrics over 5-minute windows
        trafficStream
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .aggregate(
                        TrafficMetrics::new,
                        (key, trafficData, metrics) -> metrics.update(trafficData),
                        Materialized.with(Serdes.String(), metricsSerde)
                )
                .toStream()
                .map((windowedKey, metrics) -> {
                    String key = windowedKey.key();
                    metrics.setWindowStart(windowedKey.window().start());
                    metrics.setWindowEnd(windowedKey.window().end());
                    return KeyValue.pair(key, metrics);
                })
                .to(METRICS_TOPIC, Produced.with(Serdes.String(), metricsSerde));
        
        logger.info("Metrics processing stream configured");
    }
    
    private static void processAlerts(KStream<String, TrafficData> trafficStream,
                                      JsonSerde<TrafficAlert> alertSerde,
                                      ObjectMapper objectMapper) {
        // Generate alerts for high congestion
        trafficStream
                .filter((key, data) -> data.getCongestionRatio() >= CONGESTION_THRESHOLD)
                .mapValues(data -> {
                    String severity = determineSeverity(data.getCongestionRatio());
                    
                    return TrafficAlert.builder()
                            .latitude(data.getLatitude())
                            .longitude(data.getLongitude())
                            .severity(severity)
                            .congestionRatio(data.getCongestionRatio())
                            .currentSpeed(data.getCurrentSpeed())
                            .freeFlowSpeed(data.getFreeFlowSpeed())
                            .delay(data.getDelay())
                            .roadClosure(data.isRoadClosure())
                            .timestamp(System.currentTimeMillis())
                            .message(generateAlertMessage(data, severity))
                            .build();
                })
                .to(ALERTS_TOPIC, Produced.with(Serdes.String(), alertSerde));
        
        logger.info("Alert processing stream configured");
    }
    
    private static String determineSeverity(double congestionRatio) {
        if (congestionRatio >= CRITICAL_CONGESTION_THRESHOLD) {
            return "CRITICAL";
        } else if (congestionRatio >= CONGESTION_THRESHOLD) {
            return "WARNING";
        } else {
            return "INFO";
        }
    }
    
    private static String generateAlertMessage(TrafficData data, String severity) {
        if (data.isRoadClosure()) {
            return String.format("Road closure detected at %.4f, %.4f", 
                               data.getLatitude(), data.getLongitude());
        }
        
        return String.format("%s traffic congestion (%.0f%%) at %.4f, %.4f - Speed: %.1f km/h (normal: %.1f km/h)",
                severity,
                data.getCongestionRatio() * 100,
                data.getLatitude(),
                data.getLongitude(),
                data.getCurrentSpeed(),
                data.getFreeFlowSpeed());
    }
    
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
