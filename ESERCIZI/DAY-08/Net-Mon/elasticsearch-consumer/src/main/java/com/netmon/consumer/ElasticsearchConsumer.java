package com.netmon.consumer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netmon.common.util.JsonUtil;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka consumer that indexes normalized telemetry data to Elasticsearch
 */
public class ElasticsearchConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConsumer.class);
    private static final DateTimeFormatter INDEX_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final ElasticsearchClient esClient;
    private final String indexPrefix;
    private final int batchSize;
    
    public ElasticsearchConsumer(Properties config) {
        this.indexPrefix = config.getProperty("elasticsearch.index.prefix", "network-telemetry");
        this.batchSize = Integer.parseInt(config.getProperty("consumer.batch.size", "100"));
        
        // Kafka consumer configuration
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            config.getProperty("kafka.bootstrap.servers", "localhost:9092"));
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, 
            config.getProperty("kafka.consumer.group.id", "elasticsearch-consumer-group"));
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, batchSize);
        
        this.kafkaConsumer = new KafkaConsumer<>(consumerProps);
        this.kafkaConsumer.subscribe(Collections.singletonList(
            config.getProperty("kafka.topic", "network-telemetry-normalized")
        ));
        
        // Elasticsearch client configuration
        // Read from properties first, then fall back to environment variables
        String esUrl = config.getProperty("elasticsearch.url", 
            System.getenv().getOrDefault("ELASTICSEARCH_URL", "http://localhost:9200"));
        String esUsername = config.getProperty("elasticsearch.username", 
            System.getenv("ELASTICSEARCH_USERNAME"));
        String esPassword = config.getProperty("elasticsearch.password", 
            System.getenv("ELASTICSEARCH_PASSWORD"));
        
        // Parse URL to extract host and port
        String esHost = "localhost";
        int esPort = 9200;
        String scheme = "http";
        try {
            java.net.URL url = new java.net.URL(esUrl);
            esHost = url.getHost();
            esPort = url.getPort() > 0 ? url.getPort() : 9200;
            scheme = url.getProtocol();
        } catch (Exception e) {
            logger.warn("Failed to parse Elasticsearch URL, using defaults", e);
        }
        
        // Configure Basic Authentication if credentials are provided
        var builder = RestClient.builder(new HttpHost(esHost, esPort, scheme))
            .setRequestConfigCallback(requestConfigBuilder -> 
                requestConfigBuilder
                    .setConnectTimeout(5000)
                    .setSocketTimeout(60000)
            );
        
        if (esUsername != null && esPassword != null) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(esUsername, esPassword)
            );
            
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
            
            logger.info("Elasticsearch client configured with Basic Authentication for user: {}", esUsername);
        } else {
            logger.info("Elasticsearch client configured without authentication");
        }
        
        RestClient restClient = builder.build();
        
        RestClientTransport transport = new RestClientTransport(
            restClient, 
            new JacksonJsonpMapper(objectMapper)
        );
        
        this.esClient = new ElasticsearchClient(transport);
        
        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    public void start() {
        logger.info("Starting Elasticsearch Consumer");
        
        while (running.get()) {
            try {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(5));
                
                if (!records.isEmpty()) {
                    logger.info("Received {} records", records.count());
                    processBatch(records);
                    kafkaConsumer.commitSync();
                }
                
            } catch (Exception e) {
                logger.error("Error processing records", e);
                try {
                    Thread.sleep(5000); // Backoff on error
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        logger.info("Consumer loop exited");
    }
    
    private void processBatch(ConsumerRecords<String, String> records) throws IOException {
        List<JsonNode> documents = new ArrayList<>();
        
        for (ConsumerRecord<String, String> record : records) {
            try {
                JsonNode doc = objectMapper.readTree(record.value());
                documents.add(doc);
            } catch (Exception e) {
                logger.error("Failed to parse record: {}", record.value(), e);
                // Send to dead letter queue in production
            }
        }
        
        if (!documents.isEmpty()) {
            bulkIndexDocuments(documents);
        }
    }
    
    private void bulkIndexDocuments(List<JsonNode> documents) throws IOException {
        String indexName = getCurrentIndexName();
        
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        
        for (JsonNode doc : documents) {
            String id = doc.get("normalizedId").asText();
            
            bulkBuilder.operations(op -> op
                .index(idx -> idx
                    .index(indexName)
                    .id(id)
                    .document(doc)
                )
            );
        }
        
        BulkResponse response = esClient.bulk(bulkBuilder.build());
        
        if (response.errors()) {
            logger.warn("Bulk indexing completed with errors");
            for (BulkResponseItem item : response.items()) {
                if (item.error() != null) {
                    logger.error("Failed to index document {}: {}", 
                        item.id(), item.error().reason());
                }
            }
        } else {
            logger.info("Successfully indexed {} documents to {}", 
                documents.size(), indexName);
        }
    }
    
    private String getCurrentIndexName() {
        String date = LocalDate.now().format(INDEX_DATE_FORMAT);
        return indexPrefix + "-" + date;
    }
    
    public void shutdown() {
        logger.info("Shutting down Elasticsearch Consumer");
        running.set(false);
        
        if (kafkaConsumer != null) {
            kafkaConsumer.close(Duration.ofSeconds(10));
        }
        
        if (esClient != null) {
            try {
                esClient._transport().close();
            } catch (IOException e) {
                logger.error("Error closing Elasticsearch client", e);
            }
        }
        
        logger.info("Consumer shutdown completed");
    }
    
    public static void main(String[] args) {
        String configFile = args.length > 0 ? args[0] : "../config/elasticsearch-consumer.properties";
        
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);
        } catch (IOException e) {
            logger.warn("Could not load config file: {}, using defaults", configFile);
        }
        
        ElasticsearchConsumer consumer = new ElasticsearchConsumer(config);
        consumer.start();
    }
}
