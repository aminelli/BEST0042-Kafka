package com.netmon.producer;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SwitchTelemetryProducerTest {

    @Test
    void shouldInitializeWithDefaultConfiguration() {
        // Given
        Properties config = new Properties();
        config.setProperty("kafka.bootstrap.servers", "localhost:9092");

        // When
        SwitchTelemetryProducer producer = new SwitchTelemetryProducer(config);

        // Then
        assertNotNull(producer);
    }

    @Test
    void shouldInitializeWithCustomConfiguration() {
        // Given
        Properties config = new Properties();
        config.setProperty("kafka.bootstrap.servers", "localhost:9092");
        config.setProperty("kafka.topic", "test-topic-b");
        config.setProperty("producer.min.interval.ms", "2000");
        config.setProperty("producer.max.interval.ms", "5000");
        config.setProperty("device.id", "SWITCH-TEST");
        config.setProperty("device.site", "TestSite");
        config.setProperty("device.firmware", "v1.0.0");

        // When
        SwitchTelemetryProducer producer = new SwitchTelemetryProducer(config);

        // Then
        assertNotNull(producer);
    }

    @Test
    void shouldHandleShutdownGracefully() {
        // Given
        Properties config = new Properties();
        config.setProperty("kafka.bootstrap.servers", "localhost:9092");
        SwitchTelemetryProducer producer = new SwitchTelemetryProducer(config);

        // When & Then
        assertDoesNotThrow(() -> producer.shutdown());
    }
}
