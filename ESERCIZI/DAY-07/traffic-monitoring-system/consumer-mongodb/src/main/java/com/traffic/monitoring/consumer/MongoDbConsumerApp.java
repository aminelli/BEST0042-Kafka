package com.traffic.monitoring.consumer;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application for MongoDB Consumer
 * Consumes traffic metrics and alerts from Kafka and persists to MongoDB
 */
public class MongoDbConsumerApp {
    private static final Logger logger = LoggerFactory.getLogger(MongoDbConsumerApp.class);
    
    public static void main(String[] args) {
        logger.info("Starting MongoDB Consumer...");
        
        // Load configuration from environment variables
        String kafkaBootstrapServers = getEnvOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        //String mongodbUri = getEnvOrDefault("MONGODB_URI", "mongodb://localhost:27017");
        String mongodbUri = getEnvOrDefault("MONGODB_URI", "mongodb://root:root@localhost:27017/?authSource=admin");
        
        String databaseName = getEnvOrDefault("MONGODB_DATABASE", "traffic_monitoring");
        String groupId = getEnvOrDefault("CONSUMER_GROUP_ID", "mongodb-consumer-group");
        
        String metricsTopic = getEnvOrDefault("KAFKA_TOPIC_METRICS", "traffic.metrics");
        String alertsTopic = getEnvOrDefault("KAFKA_TOPIC_ALERTS", "traffic.alerts");
        
        // Initialize MongoDB service
        MongoDbService mongoDbService = new MongoDbService(mongodbUri, databaseName);
        
        // Create consumers for each topic
        TrafficMetricsConsumer metricsConsumer = new TrafficMetricsConsumer(
                kafkaBootstrapServers, groupId + "-metrics", metricsTopic, mongoDbService);
        
        TrafficAlertsConsumer alertsConsumer = new TrafficAlertsConsumer(
                kafkaBootstrapServers, groupId + "-alerts", alertsTopic, mongoDbService);
        
        CountDownLatch latch = new CountDownLatch(2);
        
        // Start consumers in separate threads
        Thread metricsThread = new Thread(() -> {
            try {
                metricsConsumer.start();
            } finally {
                latch.countDown();
            }
        });
        
        Thread alertsThread = new Thread(() -> {
            try {
                alertsConsumer.start();
            } finally {
                latch.countDown();
            }
        });
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down MongoDB Consumer...");
            metricsConsumer.stop();
            alertsConsumer.stop();
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error("Error during shutdown", e);
            }
            
            mongoDbService.close();
            logger.info("MongoDB Consumer stopped.");
        }));
        
        // Start threads
        metricsThread.start();
        alertsThread.start();
        
        logger.info("MongoDB Consumer is running.");
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Main thread interrupted", e);
        }
    }
    
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
