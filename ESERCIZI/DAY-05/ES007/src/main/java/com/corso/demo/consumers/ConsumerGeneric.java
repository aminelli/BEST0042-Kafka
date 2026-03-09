package com.corso.demo.consumers;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

public class ConsumerGeneric {
    

    public <K, V> void loadRecors(
        String topicName,
        String groupName,
        String clientId,
        Class<? extends Deserializer<K>> keyDeserializerClass,
        Class<? extends Deserializer<V>> valueDeserializerClass
    ) {
        
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // GROUP ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);

        // CLIENT ID
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);

        // AUTO OFFSET RESET
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerClass);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);

        final Consumer<K, V> consumer = new KafkaConsumer<>(props); 
        
        consumer.subscribe(Arrays.asList(topicName));

        try {

            ConsumerRecords<K, V> records = null;

            while (true) {
                
                records = consumer.poll(Duration.ofMillis(100));

                records.forEach(record -> {
                    K key = record.key();
                    V value = record.value();

                    // Qui mettiamola logica di business per processare i record

                    System.out.printf(
                        "Topic: %s, Partition: %d, Offset: %d, key; %s, value: %s \n", 
                        topicName,
                        record.partition(), 
                        record.offset(),
                        key.toString(),
                        value.toString()
                    );
                    
                    
                });



            }

        } catch (Exception e) {
            System.out.println("Errore durante il consumo dei record");
        } finally {
            consumer.close();       
        }

    }



}
