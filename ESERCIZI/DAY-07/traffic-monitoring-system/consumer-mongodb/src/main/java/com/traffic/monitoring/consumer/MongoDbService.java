package com.traffic.monitoring.consumer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MongoDB service for managing database connections and operations
 */
public class MongoDbService {
    private static final Logger logger = LoggerFactory.getLogger(MongoDbService.class);
    
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    
    // Collection names
    public static final String METRICS_COLLECTION = "traffic_metrics";
    public static final String ALERTS_COLLECTION = "traffic_alerts";
    
    public MongoDbService(String connectionString, String databaseName) {
        logger.info("Connecting to MongoDB: {}", connectionString);
        this.mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(databaseName);
        
        // Create indexes
        createIndexes();
        
        logger.info("Connected to MongoDB database '{}'", databaseName);
    }
    
    private void createIndexes() {
        // Indexes for metrics collection
        MongoCollection<Document> metricsCollection = database.getCollection(METRICS_COLLECTION);
        
        metricsCollection.createIndex(Indexes.descending("window_start"));
        metricsCollection.createIndex(Indexes.ascending("location_key"));
        metricsCollection.createIndex(
                Indexes.compoundIndex(
                        Indexes.ascending("location_key"),
                        Indexes.descending("window_start")
                )
        );
        
        logger.info("Created indexes for '{}' collection", METRICS_COLLECTION);
        
        // Indexes for alerts collection
        MongoCollection<Document> alertsCollection = database.getCollection(ALERTS_COLLECTION);
        
        alertsCollection.createIndex(Indexes.descending("timestamp"));
        alertsCollection.createIndex(Indexes.ascending("severity"));
        alertsCollection.createIndex(
                Indexes.geo2dsphere("location")
        );
        alertsCollection.createIndex(
                Indexes.compoundIndex(
                        Indexes.ascending("severity"),
                        Indexes.descending("timestamp")
                )
        );
        
        logger.info("Created indexes for '{}' collection", ALERTS_COLLECTION);
    }
    
    /**
     * Insert traffic metrics documents in batch
     */
    public void insertMetrics(List<Document> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return;
        }
        
        try {
            MongoCollection<Document> collection = database.getCollection(METRICS_COLLECTION);
            collection.insertMany(metrics);
            logger.debug("Inserted {} metrics documents", metrics.size());
        } catch (Exception e) {
            logger.error("Error inserting metrics to MongoDB", e);
        }
    }
    
    /**
     * Insert single traffic metrics document
     */
    public void insertMetric(Document metric) {
        try {
            MongoCollection<Document> collection = database.getCollection(METRICS_COLLECTION);
            collection.insertOne(metric);
            logger.debug("Inserted metric document");
        } catch (Exception e) {
            logger.error("Error inserting metric to MongoDB", e);
        }
    }
    
    /**
     * Insert traffic alerts documents in batch
     */
    public void insertAlerts(List<Document> alerts) {
        if (alerts == null || alerts.isEmpty()) {
            return;
        }
        
        try {
            MongoCollection<Document> collection = database.getCollection(ALERTS_COLLECTION);
            collection.insertMany(alerts);
            logger.debug("Inserted {} alert documents", alerts.size());
        } catch (Exception e) {
            logger.error("Error inserting alerts to MongoDB", e);
        }
    }
    
    /**
     * Insert single traffic alert document
     */
    public void insertAlert(Document alert) {
        try {
            MongoCollection<Document> collection = database.getCollection(ALERTS_COLLECTION);
            collection.insertOne(alert);
            logger.debug("Inserted alert document");
        } catch (Exception e) {
            logger.error("Error inserting alert to MongoDB", e);
        }
    }
    
    /**
     * Close MongoDB connection
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB connection closed");
        }
    }
}
