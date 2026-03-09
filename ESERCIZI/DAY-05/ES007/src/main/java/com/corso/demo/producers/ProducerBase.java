package com.corso.demo.producers;

import org.apache.kafka.clients.producer.RecordMetadata;

public class ProducerBase {

    protected void printMetadata(String key, RecordMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("Messaggio ")
            .append(key).append(" inviato al topic: ")
            .append(metadata.topic())
            .append(" - partition: ")
            .append(metadata.partition())
            .append(" - offset: ")
            .append(metadata.offset())
            .append(" - timestamp: ")
            .append(metadata.timestamp());

        System.out.println(sb.toString());    

        
    }
    
}
