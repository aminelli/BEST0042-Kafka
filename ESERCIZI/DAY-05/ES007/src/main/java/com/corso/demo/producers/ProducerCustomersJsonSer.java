package com.corso.demo.producers;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import com.corso.demo.models.Customer;
import com.corso.demo.serializers.SerializerJsonCustomer;
import com.corso.demo.serializers.SerializerJsonModel;

import net.datafaker.Faker;

public class ProducerCustomersJsonSer extends ProducerBase {
    
    
    public void sendMessagesJsonCustomer(String topicName, int maxMessages) {

        Faker faker = new Faker();

        Properties props = new Properties();

        // props.put("bootstrap.servers", "localhost:9092");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Client ID
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "Producer002");


        // Fattore di compressione dei messaggi
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

        // Tipo Acks
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        // Namespaces delle CLASSI da utilizzare per la serializzazione di chiave e valore
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SerializerJsonCustomer.class.getName());
      

        // Retries
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        try (final KafkaProducer<String, Customer> producer = new KafkaProducer<>(props)) {
           
            ProducerRecord<String, Customer> record = null;
            Customer customer = null;
            
            for (int counter = 0; counter < maxMessages; counter++) {

                customer = new Customer(
                    faker.idNumber().valid(),
                    faker.name().firstName(),
                    faker.name().lastName(),
                    faker.internet().emailAddress()
                );

                String key = "K" + counter;
                Customer value = customer;
                
                record = new ProducerRecord<>(topicName, key, value);
                String headerInfo = "MSG KEY: " + record.key();
                record.headers().add("headerKey", headerInfo.getBytes());
                

                try {
                   
                   Future<RecordMetadata> future = producer.send(record);
                   RecordMetadata metadata = future.get();
                   printMetadata(key, metadata);

                    
                } catch (Exception ex) {
                    System.out.println("Errore durante l'invio del messaggio: " + counter);
                }
                
            }
           
            producer.flush();
            producer.close();

        }

    }


    public void sendMessagesJsonGeneric(String topicName, int maxMessages) {

        Faker faker = new Faker();

        Properties props = new Properties();

        // props.put("bootstrap.servers", "localhost:9092");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Client ID
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "Producer002");


        // Fattore di compressione dei messaggi
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

        // Tipo Acks
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        // Namespaces delle CLASSI da utilizzare per la serializzazione di chiave e valore
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SerializerJsonModel.class.getName());
      

        // Retries
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        try (final KafkaProducer<String, Customer> producer = new KafkaProducer<>(props)) {
           
            ProducerRecord<String, Customer> record = null;
            Customer customer = null;
            
            for (int counter = 0; counter < maxMessages; counter++) {

                customer = new Customer(
                    faker.idNumber().valid(),
                    faker.name().firstName(),
                    faker.name().lastName(),
                    faker.internet().emailAddress()
                );

                String key = "K" + counter;
                Customer value = customer;
                
                record = new ProducerRecord<>(topicName, key, value);
                String headerInfo = "MSG KEY: " + record.key();
                record.headers().add("headerKey", headerInfo.getBytes());
                

                try {
                   
                   Future<RecordMetadata> future = producer.send(record);
                   RecordMetadata metadata = future.get();
                   printMetadata(key, metadata);

                    
                } catch (Exception ex) {
                    System.out.println("Errore durante l'invio del messaggio: " + counter);
                }
                
            }
           
            producer.flush();
            producer.close();

        }

    }

}
