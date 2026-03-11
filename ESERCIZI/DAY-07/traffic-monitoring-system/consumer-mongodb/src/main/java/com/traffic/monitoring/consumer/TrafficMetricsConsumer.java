package com.traffic.monitoring.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka Consumer for traffic metrics
 */
public class TrafficMetricsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(TrafficMetricsConsumer.class);
    private static final int BATCH_SIZE = 100;
    
    private final KafkaConsumer<String, String> consumer;
    private final MongoDbService mongoDbService;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    public TrafficMetricsConsumer(String bootstrapServers, String groupId, 
                                  String topic, MongoDbService mongoDbService) {
        this.consumer = createConsumer(bootstrapServers, groupId);
        this.consumer.subscribe(Collections.singletonList(topic));
        this.mongoDbService = mongoDbService;
        this.objectMapper = new ObjectMapper();
        
        logger.info("Traffic Metrics Consumer initialized for topic '{}'", topic);
    }
    
    private KafkaConsumer<String, String> createConsumer(String bootstrapServers, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // Consumer settings
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, BATCH_SIZE);
        
        // Performance optimization
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        return new KafkaConsumer<>(props);
    }
    
    public void start() {
        running.set(true);
        logger.info("Starting Traffic Metrics Consumer...");
        
        List<Document> batch = new ArrayList<>();
        
        while (running.get()) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        Document document = Document.parse(record.value());
                        batch.add(document);
                        
                        // Insert batch if size reached
                        if (batch.size() >= BATCH_SIZE) {
                            mongoDbService.insertMetrics(batch);
                            logger.info("Inserted batch of {} metrics to MongoDB", batch.size());
                            batch.clear();
                            consumer.commitSync();
                        }
                        
                    } catch (Exception e) {
                        logger.error("Error processing metric record", e);
                    }
                }
                
                // Insert remaining records in batch
                if (!batch.isEmpty()) {
                    mongoDbService.insertMetrics(batch);
                    logger.info("Inserted batch of {} metrics to MongoDB", batch.size());
                    batch.clear();
                    consumer.commitSync();
                }
                
            } catch (Exception e) {
                logger.error("Error in consumer poll loop", e);
            }
        }
        
        // Final batch insert
        if (!batch.isEmpty()) {
            mongoDbService.insertMetrics(batch);
            logger.info("Inserted final batch of {} metrics to MongoDB", batch.size());
        }
        
        consumer.close();
        logger.info("Traffic Metrics Consumer stopped");
    }
    
    public void stop() {
        logger.info("Stopping Traffic Metrics Consumer...");
        running.set(false);
        consumer.wakeup();
    }
}
