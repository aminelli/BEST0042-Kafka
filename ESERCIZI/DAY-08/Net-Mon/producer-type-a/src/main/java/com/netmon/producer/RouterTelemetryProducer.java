package com.netmon.producer;

import com.netmon.common.model.RouterTelemetry;
import com.netmon.common.util.JsonUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka producer for Router telemetry data (Type A)
 * Simulates network telemetry from router devices
 */
public class RouterTelemetryProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(RouterTelemetryProducer.class);
    private static final Random random = new Random();
    private final AtomicBoolean running = new AtomicBoolean(true);
    
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final int minIntervalMs;
    private final int maxIntervalMs;
    private final String deviceId;
    private final String location;
    
    public RouterTelemetryProducer(Properties config) {
        this.topic = config.getProperty("kafka.topic", "network-telemetry-type-a");
        this.minIntervalMs = Integer.parseInt(config.getProperty("producer.min.interval.ms", "2000"));
        this.maxIntervalMs = Integer.parseInt(config.getProperty("producer.max.interval.ms", "5000"));
        this.deviceId = config.getProperty("device.id", "ROUTER-001");
        this.location = config.getProperty("device.location", "DataCenter-1");
        
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            config.getProperty("kafka.bootstrap.servers", "localhost:9092"));
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        producerProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        this.producer = new KafkaProducer<>(producerProps);
        
        // Shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    public void start() {
        logger.info("Starting Router Telemetry Producer for device: {}", deviceId);
        
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        executor.submit(() -> {
            while (running.get()) {
                try {
                    RouterTelemetry telemetry = generateTelemetry();
                    sendTelemetry(telemetry);
                    
                    int sleepTime = minIntervalMs + random.nextInt(maxIntervalMs - minIntervalMs);
                    Thread.sleep(sleepTime);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Producer interrupted");
                    break;
                } catch (Exception e) {
                    logger.error("Error producing telemetry", e);
                    exponentialBackoff();
                }
            }
        });
        
        logger.info("Producer started successfully");
    }
    
    private RouterTelemetry generateTelemetry() {
        // Simulate realistic network metrics with random variations
        long baseBytes = 1_000_000L;
        long bytesIn = baseBytes + random.nextLong(500_000);
        long bytesOut = (long) (bytesIn * (0.8 + random.nextDouble() * 0.4));
        
        long packetsIn = bytesIn / 64 + random.nextLong(1000);
        long packetsOut = bytesOut / 64 + random.nextLong(1000);
        
        double errorRate = random.nextDouble() * 0.05; // 0-5% error rate
        
        List<RouterTelemetry.NetworkInterface> interfaces = List.of(
            new RouterTelemetry.NetworkInterface(
                "eth0", 
                random.nextDouble() > 0.95 ? "DOWN" : "UP", 
                1000
            ),
            new RouterTelemetry.NetworkInterface(
                "eth1", 
                random.nextDouble() > 0.98 ? "DOWN" : "UP", 
                1000
            )
        );
        
        RouterTelemetry.RouterMetrics metrics = new RouterTelemetry.RouterMetrics(
            bytesIn, bytesOut, packetsIn, packetsOut, errorRate, interfaces
        );
        
        return new RouterTelemetry(
            deviceId,
            Instant.now(),
            "router",
            metrics,
            location
        );
    }
    
    private void sendTelemetry(RouterTelemetry telemetry) {
        String json = JsonUtil.toJson(telemetry);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, deviceId, json);
        
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                logger.error("Failed to send telemetry for device: {}", deviceId, exception);
            } else {
                logger.debug("Telemetry sent successfully to partition {} at offset {}", 
                    metadata.partition(), metadata.offset());
            }
        });
    }
    
    private void exponentialBackoff() {
        try {
            int backoffMs = 1000 + random.nextInt(2000);
            logger.info("Backing off for {} ms", backoffMs);
            Thread.sleep(backoffMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void shutdown() {
        logger.info("Shutting down Router Telemetry Producer");
        running.set(false);
        
        if (producer != null) {
            producer.flush();
            producer.close(Duration.ofSeconds(5));
        }
        
        logger.info("Producer shutdown completed");
    }
    
    public static void main(String[] args) {
        String configFile = args.length > 0 ? args[0] : "../config/producer-type-a.properties";
        
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);
        } catch (IOException e) {
            logger.warn("Could not load config file: {}, using defaults", configFile);
        }
        
        RouterTelemetryProducer producer = new RouterTelemetryProducer(config);
        producer.start();
        
        // Keep main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
