package com.ecommerce.streaming;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Sink personalizzato per scrivere le aggregazioni su MongoDB.
 * Utilizza il driver MongoDB ufficiale per Java.
 */
public class MongoDBSink extends RichSinkFunction<OrderAggregation> {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBSink.class);
    
    private final String connectionString;
    private final String databaseName;
    private final String collectionName;
    
    private transient MongoClient mongoClient;
    private transient MongoCollection<Document> collection;
    
    /**
     * Costruttore del sink MongoDB.
     * 
     * @param connectionString Stringa di connessione MongoDB (es: mongodb://user:pass@host:port)
     * @param databaseName Nome del database
     * @param collectionName Nome della collezione dove salvare le aggregazioni
     */
    public MongoDBSink(String connectionString, String databaseName, String collectionName) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
    }
    
    /**
     * Metodo chiamato all'inizializzazione del sink.
     * Crea la connessione al database MongoDB.
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        LOG.info("Inizializzazione connessione MongoDB...");
        LOG.info("Database: {}, Collection: {}", databaseName, collectionName);
        
        try {
            // Creazione client MongoDB
            mongoClient = MongoClients.create(connectionString);
            
            // Selezione database e collezione
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            collection = database.getCollection(collectionName);
            
            LOG.info("✓ Connessione MongoDB stabilita con successo");
            
        } catch (Exception e) {
            LOG.error("✗ Errore nella connessione a MongoDB", e);
            throw e;
        }
    }
    
    /**
     * Metodo chiamato per ogni elemento da scrivere.
     * Converte l'aggregazione in un documento MongoDB e lo inserisce.
     */
    @Override
    public void invoke(OrderAggregation aggregation, Context context) throws Exception {
        try {
            // Creazione documento MongoDB dall'aggregazione
            Document doc = new Document()
                .append("window_start", Instant.ofEpochMilli(aggregation.getWindowStart()))
                .append("window_end", Instant.ofEpochMilli(aggregation.getWindowEnd()))
                .append("total_orders", aggregation.getTotalOrders())
                .append("total_revenue", aggregation.getTotalRevenue())
                .append("average_order_value", aggregation.getAverageOrderValue())
                .append("max_order_value", aggregation.getMaxOrderValue())
                .append("min_order_value", aggregation.getMinOrderValue() == Double.MAX_VALUE ? 
                    0.0 : aggregation.getMinOrderValue())
                .append("status_breakdown", new Document()
                    .append("pending", aggregation.getPendingOrders())
                    .append("processing", aggregation.getProcessingOrders())
                    .append("shipped", aggregation.getShippedOrders())
                    .append("delivered", aggregation.getDeliveredOrders())
                    .append("cancelled", aggregation.getCancelledOrders()))
                .append("processing_time", Instant.ofEpochMilli(aggregation.getProcessingTime()))
                .append("created_at", Instant.now());
            
            // Inserimento nel database
            collection.insertOne(doc);
            
            LOG.info("✓ Aggregazione salvata su MongoDB: {}", aggregation);
            
        } catch (Exception e) {
            LOG.error("✗ Errore nell'inserimento su MongoDB", e);
            // In produzione, si potrebbe implementare una logica di retry o DLQ
            throw e;
        }
    }
    
    /**
     * Metodo chiamato alla chiusura del sink.
     * Chiude la connessione MongoDB.
     */
    @Override
    public void close() throws Exception {
        super.close();
        
        if (mongoClient != null) {
            LOG.info("Chiusura connessione MongoDB...");
            mongoClient.close();
            LOG.info("✓ Connessione MongoDB chiusa");
        }
    }
}
