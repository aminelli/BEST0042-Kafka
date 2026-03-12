package com.netmon.producer;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class RouterTelemetryProducerTest {

    @Test
    void shouldInitializeWithDefaultConfiguration() {
        // Given
        Properties config = new Properties();
        config.setProperty("kafka.bootstrap.servers", "localhost:9092");

        // When
        RouterTelemetryProducer producer = new RouterTelemetryProducer(config);

        // Then
        assertNotNull(producer);
    }

    @Test
    void shouldInitializeWithCustomConfiguration() {
        // Given
        Properties config = new Properties();
        config.setProperty("kafka.bootstrap.servers", "localhost:9092");
        config.setProperty("kafka.topic", "test-topic");
        config.setProperty("producer.min.interval.ms", "1000");
        config.setProperty("producer.max.interval.ms", "3000");
        config.setProperty("device.id", "ROUTER-TEST");
        config.setProperty("device.location", "TestLab");

        // When
        RouterTelemetryProducer producer = new RouterTelemetryProducer(config);

        // Then
        assertNotNull(producer);
    }

    @Test
    void shouldHandleShutdownGracefully() {
        // Given
        Properties config = new Properties();
        config.setProperty("kafka.bootstrap.servers", "localhost:9092");
        RouterTelemetryProducer producer = new RouterTelemetryProducer(config);

        // When & Then
        assertDoesNotThrow(() -> producer.shutdown());
    }
}
