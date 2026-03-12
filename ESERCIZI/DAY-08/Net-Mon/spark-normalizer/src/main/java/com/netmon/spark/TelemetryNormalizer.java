package com.netmon.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netmon.common.model.NormalizedTelemetry;
import com.netmon.common.model.RouterTelemetry;
import com.netmon.common.model.SwitchTelemetry;
import com.netmon.common.util.JsonUtil;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Spark Structured Streaming application to normalize telemetry data from different sources
 */
public class TelemetryNormalizer {
    
    private static final Logger logger = LoggerFactory.getLogger(TelemetryNormalizer.class);
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    
    public static void main(String[] args) throws Exception {
        String configFile = args.length > 0 ? args[0] : "../config/spark-normalizer.properties";
        
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);
        } catch (IOException e) {
            logger.warn("Could not load config file: {}, using defaults", configFile);
        }
        
        // Create Spark Session
        SparkSession spark = SparkSession.builder()
            .appName("Network Telemetry Normalizer")
            .master(config.getProperty("spark.master", "local[*]"))
            .config("spark.sql.streaming.checkpointLocation", 
                config.getProperty("spark.checkpoint.location", "checkpoint"))
            .getOrCreate();
        
        spark.sparkContext().setLogLevel("WARN");
        
        String kafkaBootstrap = config.getProperty("kafka.bootstrap.servers", "localhost:9092");
        String inputTopicA = config.getProperty("kafka.input.topic.a", "network-telemetry-type-a");
        String inputTopicB = config.getProperty("kafka.input.topic.b", "network-telemetry-type-b");
        String outputTopic = config.getProperty("kafka.output.topic", "network-telemetry-normalized");
        
        try {
            // Read from both input topics
            Dataset<Row> streamA = readFromKafka(spark, kafkaBootstrap, inputTopicA, "typeA");
            Dataset<Row> streamB = readFromKafka(spark, kafkaBootstrap, inputTopicB, "typeB");
            
            // Union both streams
            Dataset<Row> combinedStream = streamA.union(streamB);
            
            // Normalize the data
            Dataset<Row> normalizedStream = normalizeData(spark, combinedStream);
            
            // Write to output Kafka topic
            StreamingQuery query = writeToKafka(normalizedStream, kafkaBootstrap, outputTopic);
            
            logger.info("Spark Streaming job started successfully");
            query.awaitTermination();
            
        } catch (Exception e) {
            logger.error("Error in Spark streaming job", e);
            throw e;
        } finally {
            spark.stop();
        }
    }
    
    private static Dataset<Row> readFromKafka(SparkSession spark, String bootstrap, 
                                              String topic, String sourceType) {
        Dataset<Row> df = spark.readStream()
            .format("kafka")
            .option("kafka.bootstrap.servers", bootstrap)
            .option("subscribe", topic)
            .option("startingOffsets", "latest")
            .option("maxOffsetsPerTrigger", 1000)
            .load();
        
        // Add source type column
        return df.selectExpr(
            "CAST(value AS STRING) as json_data",
            "timestamp as kafka_timestamp"
        ).withColumn("source_type", functions.lit(sourceType));
    }
    
    private static Dataset<Row> normalizeData(SparkSession spark, Dataset<Row> input) {
        // Register UDF for normalization
        UDF2<String, String, String> normalizeUdf = (jsonData, sourceType) -> 
            normalizeJsonData(jsonData, sourceType);
        
        spark.udf().register("normalizeJson", normalizeUdf, DataTypes.StringType);
        
        return input.selectExpr(
            "normalizeJson(json_data, source_type) as normalized_json"
        );
    }
    
    private static String normalizeJsonData(String jsonData, String sourceType) {
        try {
            NormalizedTelemetry normalized;
            
            if ("typeA".equals(sourceType)) {
                normalized = normalizeRouterData(jsonData);
            } else if ("typeB".equals(sourceType)) {
                normalized = normalizeSwitchData(jsonData);
            } else {
                throw new IllegalArgumentException("Unknown source type: " + sourceType);
            }
            
            return JsonUtil.toJson(normalized);
            
        } catch (Exception e) {
            logger.error("Failed to normalize data from source: {}", sourceType, e);
            return null;
        }
    }
    
    private static NormalizedTelemetry normalizeRouterData(String jsonData) throws Exception {
        RouterTelemetry router = objectMapper.readValue(jsonData, RouterTelemetry.class);
        
        long totalBytes = router.metrics().bytesIn() + router.metrics().bytesOut();
        long totalPackets = router.metrics().packetsIn() + router.metrics().packetsOut();
        
        NormalizedTelemetry.NormalizedMetrics metrics = new NormalizedTelemetry.NormalizedMetrics(
            totalBytes,
            totalPackets,
            router.metrics().errorRate(),
            null, // utilization not available in router data
            null  // activeConnections not available in router data
        );
        
        Map<String, Object> rawData = objectMapper.readValue(jsonData, Map.class);
        
        return new NormalizedTelemetry(
            UUID.randomUUID().toString(),
            router.deviceId(),
            router.deviceType(),
            router.timestamp(),
            router.location(),
            metrics,
            rawData,
            Instant.now(),
            "1.0"
        );
    }
    
    private static NormalizedTelemetry normalizeSwitchData(String jsonData) throws Exception {
        SwitchTelemetry switchData = objectMapper.readValue(jsonData, SwitchTelemetry.class);
        
        NormalizedTelemetry.NormalizedMetrics metrics = new NormalizedTelemetry.NormalizedMetrics(
            switchData.stats().totalTraffic(),
            null, // totalPackets not directly available in switch data
            null, // errorRate calculated from dropped packets
            switchData.stats().utilization(),
            switchData.stats().activeConnections()
        );
        
        // Convert timestamp from milliseconds
        Instant timestamp = Instant.ofEpochMilli(switchData.ts());
        
        Map<String, Object> rawData = objectMapper.readValue(jsonData, Map.class);
        
        return new NormalizedTelemetry(
            UUID.randomUUID().toString(),
            switchData.id(),
            switchData.type(),
            timestamp,
            switchData.site(),
            metrics,
            rawData,
            Instant.now(),
            "1.0"
        );
    }
    
    private static StreamingQuery writeToKafka(Dataset<Row> output, String bootstrap, String topic) 
            throws Exception {
        return output
            .filter(functions.col("normalized_json").isNotNull())
            .selectExpr("CAST(normalized_json AS STRING) as value")
            .writeStream()
            .format("kafka")
            .option("kafka.bootstrap.servers", bootstrap)
            .option("topic", topic)
            .option("checkpointLocation", "checkpoint/output")
            .trigger(Trigger.ProcessingTime("5 seconds"))
            .start();
    }
}
