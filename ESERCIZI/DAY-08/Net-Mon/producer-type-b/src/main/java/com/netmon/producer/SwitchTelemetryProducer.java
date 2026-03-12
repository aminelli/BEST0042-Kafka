package com.netmon.producer;

import com.netmon.common.model.SwitchTelemetry;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka producer for Switch telemetry data (Type B)
 * Simulates network telemetry from switch devices with different payload structure
 */
public class SwitchTelemetryProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(SwitchTelemetryProducer.class);
    private static final Random random = new Random();
    private final AtomicBoolean running = new AtomicBoolean(true);
    
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final int minIntervalMs;
    private final int maxIntervalMs;
    private final String deviceId;
    private final String site;
    private final String firmware;
    
    public SwitchTelemetryProducer(Properties config) {
        this.topic = config.getProperty("kafka.topic", "network-telemetry-type-b");
        this.minIntervalMs = Integer.parseInt(config.getProperty("producer.min.interval.ms", "3000"));
        this.maxIntervalMs = Integer.parseInt(config.getProperty("producer.max.interval.ms", "7000"));
        this.deviceId = config.getProperty("device.id", "SWITCH-042");
        this.site = config.getProperty("device.site", "Office-Building-2");
        this.firmware = config.getProperty("device.firmware", "v2.3.1");
        
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            config.getProperty("kafka.bootstrap.servers", "localhost:9092"));
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        producerProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        this.producer = new KafkaProducer<>(producerProps);
        
        // Shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    public void start() {
        logger.info("Starting Switch Telemetry Producer for device: {}", deviceId);
        
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        executor.submit(() -> {
            while (running.get()) {
                try {
                    SwitchTelemetry telemetry = generateTelemetry();
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
    
    private SwitchTelemetry generateTelemetry() {
        // Simulate realistic switch metrics with occasional anomalies
        long baseTotalTraffic = 2_000_000L;
        
        // Occasionally simulate traffic spikes (10% chance)
        boolean spike = random.nextDouble() < 0.10;
        long totalTraffic = spike ? 
            baseTotalTraffic * (2 + random.nextInt(3)) : 
            baseTotalTraffic + random.nextLong(500_000);
        
        int activeConnections = 200 + random.nextInt(100);
        
        // Occasionally simulate packet loss (5% chance of higher drops)
        int droppedPackets = random.nextDouble() < 0.05 ? 
            50 + random.nextInt(100) : 
            random.nextInt(20);
        
        double utilization = Math.min(95.0, 
            (totalTraffic / (double) (baseTotalTraffic * 3)) * 100);
        
        // Generate VLAN statistics
        Map<String, SwitchTelemetry.VlanStats> vlanStats = new HashMap<>();
        vlanStats.put("vlan10", new SwitchTelemetry.VlanStats(
            totalTraffic / 3 + random.nextLong(100_000),
            40 + random.nextInt(20)
        ));
        vlanStats.put("vlan20", new SwitchTelemetry.VlanStats(
            totalTraffic / 4 + random.nextLong(100_000),
            30 + random.nextInt(15)
        ));
        
        SwitchTelemetry.SwitchStats stats = new SwitchTelemetry.SwitchStats(
            totalTraffic,
            activeConnections,
            droppedPackets,
            utilization,
            vlanStats
        );
        
        return new SwitchTelemetry(
            deviceId,
            System.currentTimeMillis(),
            "switch",
            stats,
            site,
            firmware
        );
    }
    
    private void sendTelemetry(SwitchTelemetry telemetry) {
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
        logger.info("Shutting down Switch Telemetry Producer");
        running.set(false);
        
        if (producer != null) {
            producer.flush();
            producer.close(Duration.ofSeconds(5));
        }
        
        logger.info("Producer shutdown completed");
    }
    
    public static void main(String[] args) {
        String configFile = args.length > 0 ? args[0] : "../config/producer-type-b.properties";
        
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);
        } catch (IOException e) {
            logger.warn("Could not load config file: {}, using defaults", configFile);
        }
        
        SwitchTelemetryProducer producer = new SwitchTelemetryProducer(config);
        producer.start();
        
        // Keep main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
