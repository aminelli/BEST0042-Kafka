package com.corso.demo.partitioners;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

public class PartitionerCustom implements Partitioner {

    @Override
    public void configure(java.util.Map<String, ?> configs) {
        // Configurazione del partitioner, se necessaria
        System.out.println("PartitionerCustom configurato con: " + configs);
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // Logica di partizionamento personalizzata basata sulla chiave
        // Ad esempio, puoi usare l'hash code della chiave per determinare la partizione
        //return Math.abs(key.hashCode()) % cluster.partitionCountForTopic(topic);

        if (key != null && key instanceof String) {
            String keyStr = (String) key;

            if (keyStr.startsWith("JSON")) {
                // Assegna a una partizione specifica per JSON
                return 1; // Partizione 0 per JSON
            } else if (keyStr.startsWith("XML")) {
                // Assegna a una partizione specifica per XML
                return 2; // Partizione 1 per XML
            } else if (keyStr.startsWith("CSV")) {
                // Assegna a una partizione specifica per CSV
                return 3; // Partizione 2 per CSV
            }
            return 0;
            
        } else {
            // Se la chiave è null o non è una stringa, usa una partizione di default
            return 0;
        }


    }

    @Override
    public void close() {
        // Pulizia delle risorse, se necessaria
        System.out.println("PartitionerCustom chiuso.");
    }



}




