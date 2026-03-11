package com.traffic.monitoring.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Kafka Producer service for publishing traffic data
 */
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    
    private final KafkaProducer<String, String> producer;
    private final String topicName;
    private final ObjectMapper objectMapper;
    
    public KafkaProducerService(String bootstrapServers, String topicName) {
        this.topicName = topicName;
        this.producer = createProducer(bootstrapServers);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        logger.info("Kafka Producer initialized for topic '{}' with bootstrap servers '{}'", 
                   topicName, bootstrapServers);
    }
    
    private KafkaProducer<String, String> createProducer(String bootstrapServers) {
        Properties props = new Properties();
        
        // Basic configuration
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Producer reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for leader acknowledgment
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        
        // Performance optimization
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        // Client ID for monitoring
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "tomtom-producer");
        
        return new KafkaProducer<>(props);
    }
    
    /**
     * Send traffic data to Kafka topic
     */
    public void send(TrafficData trafficData) {
        try {
            // Generate key based on location (for partitioning)
            String key = generateKey(trafficData);
            
            // Serialize to JSON
            String value = objectMapper.writeValueAsString(trafficData);
            
            // Create producer record
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);
            
            // Send asynchronously with callback
            producer.send(record, (RecordMetadata metadata, Exception exception) -> {
                if (exception != null) {
                    logger.error("Error sending message to Kafka for key '{}'", key, exception);
                } else {
                    logger.debug("Message sent successfully: topic={}, partition={}, offset={}", 
                               metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
            
        } catch (Exception e) {
            logger.error("Error preparing message for Kafka", e);
        }
    }
    
    /**
     * Generate partition key based on geographic coordinates
     * This ensures data from the same area goes to the same partition
     */
    private String generateKey(TrafficData data) {
        // Round coordinates to 2 decimal places for grouping
        int latKey = (int) (data.getLatitude() * 100);
        int lonKey = (int) (data.getLongitude() * 100);
        return String.format("%d_%d", latKey, lonKey);
    }
    
    /**
     * Flush and close producer
     */
    public void close() {
        logger.info("Closing Kafka Producer...");
        if (producer != null) {
            producer.flush();
            producer.close();
        }
        logger.info("Kafka Producer closed");
    }
}
