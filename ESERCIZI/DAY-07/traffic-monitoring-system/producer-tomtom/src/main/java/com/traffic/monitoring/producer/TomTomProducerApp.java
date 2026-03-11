package com.traffic.monitoring.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main application for TomTom Traffic Data Producer
 * Polls TomTom API and publishes traffic data to Kafka topics
 */
public class TomTomProducerApp {
    private static final Logger logger = LoggerFactory.getLogger(TomTomProducerApp.class);
    
    public static void main(String[] args) {
        logger.info("Starting TomTom Traffic Producer...");
        
        // Load configuration from environment variables
        String tomtomApiKey = getEnvOrDefault("TOMTOM_API_KEY", "");
        String kafkaBootstrapServers = getEnvOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String topicName = getEnvOrDefault("KAFKA_TOPIC_TRAFFIC_RAW", "traffic.raw");
        int pollingIntervalSeconds = Integer.parseInt(getEnvOrDefault("POLLING_INTERVAL_SECONDS", "60"));
        
        // Geographic bounding box for traffic data (example: Rome, Italy)
        double minLat = Double.parseDouble(getEnvOrDefault("MIN_LAT", "41.8"));
        double minLon = Double.parseDouble(getEnvOrDefault("MIN_LON", "12.4"));
        double maxLat = Double.parseDouble(getEnvOrDefault("MAX_LAT", "42.0"));
        double maxLon = Double.parseDouble(getEnvOrDefault("MAX_LON", "12.6"));
        
        if (tomtomApiKey.isEmpty()) {
            logger.error("TOMTOM_API_KEY environment variable is not set!");
            System.exit(1);
        }
        
        // Initialize components
        TomTomClient tomtomClient = new TomTomClient(tomtomApiKey);
        KafkaProducerService kafkaProducer = new KafkaProducerService(kafkaBootstrapServers, topicName);
        
        // Setup scheduled polling
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down TomTom Producer...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            kafkaProducer.close();
            logger.info("TomTom Producer stopped.");
        }));
        
        // Schedule polling task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("Fetching traffic data from TomTom API...");
                var trafficData = tomtomClient.fetchTrafficFlow(minLat, minLon, maxLat, maxLon);
                
                if (trafficData != null && !trafficData.isEmpty()) {
                    logger.info("Fetched {} traffic flow records", trafficData.size());
                    
                    // Publish each record to Kafka
                    for (var data : trafficData) {
                        kafkaProducer.send(data);
                    }
                    
                    logger.info("Successfully published {} records to Kafka topic '{}'", 
                               trafficData.size(), topicName);
                } else {
                    logger.warn("No traffic data received from TomTom API");
                }
                
            } catch (Exception e) {
                logger.error("Error during polling cycle", e);
            }
        }, 0, pollingIntervalSeconds, TimeUnit.SECONDS);
        
        logger.info("TomTom Producer is running. Polling every {} seconds", pollingIntervalSeconds);
    }
    
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
