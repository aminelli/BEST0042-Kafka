package com.ecommerce.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MapFunction serializzabile per il parsing di JSON Debezium in oggetti Java.
 * Ogni task worker crea il proprio ObjectMapper per evitare problemi di serializzazione.
 */
public class JsonParser implements MapFunction<String, DebeziumCdcEvent> {
    
    private static final Logger LOG = LoggerFactory.getLogger(JsonParser.class);
    
    // transient = non viene serializzato, viene ricreato su ogni worker
    private transient ObjectMapper objectMapper;
    
    /**
     * Inizializzazione dell'ObjectMapper.
     * Chiamato una volta per ogni task worker.
     */
    private void ensureInitialized() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
        }
    }
    
    @Override
    public DebeziumCdcEvent map(String jsonString) throws Exception {
        ensureInitialized();
        
        try {
            return objectMapper.readValue(jsonString, DebeziumCdcEvent.class);
        } catch (Exception e) {
            LOG.warn("Errore nel parsing JSON: {}", jsonString, e);
            return null;
        }
    }
}
