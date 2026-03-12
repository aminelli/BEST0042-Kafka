package com.netmon.consumer;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ElasticsearchConsumerTest {

    @Test
    void shouldInitializeWithDefaultConfiguration() {
        // Given
        Properties config = new Properties();
        config.setProperty("kafka.bootstrap.servers", "localhost:9092");
        config.setProperty("elasticsearch.host", "localhost");
        config.setProperty("elasticsearch.port", "9200");

        // When
        ElasticsearchConsumer consumer = new ElasticsearchConsumer(config);

        // Then
        assertNotNull(consumer);
    }

    @Test
    void shouldInitializeWithCustomConfiguration() {
        // Given
        Properties config = new Properties();
        config.setProperty("kafka.bootstrap.servers", "localhost:9092");
        config.setProperty("kafka.topic", "test-normalized");
        config.setProperty("kafka.consumer.group.id", "test-group");
        config.setProperty("elasticsearch.host", "localhost");
        config.setProperty("elasticsearch.port", "9200");
        config.setProperty("elasticsearch.index.prefix", "test-telemetry");
        config.setProperty("consumer.batch.size", "50");

        // When
        ElasticsearchConsumer consumer = new ElasticsearchConsumer(config);

        // Then
        assertNotNull(consumer);
    }

    @Test
    void shouldHandleShutdownGracefully() {
        // Given
        Properties config = new Properties();
        config.setProperty("kafka.bootstrap.servers", "localhost:9092");
        config.setProperty("elasticsearch.host", "localhost");
        config.setProperty("elasticsearch.port", "9200");
        ElasticsearchConsumer consumer = new ElasticsearchConsumer(config);

        // When & Then
        assertDoesNotThrow(() -> consumer.shutdown());
    }
}
