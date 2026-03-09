package com.corso.demo.producers;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerAsyncAcksAll extends ProducerBase {

    public void sendMessages(String topicName, int maxMessages) {

        Properties props = new Properties();

        // props.put("bootstrap.servers", "localhost:9092");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // Client ID
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "ProducerAsyncX");

        // Fattore di compressione dei messaggi
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

        // Tipo Acks
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // Namespaces delle CLASSI da utilizzare per la serializzazione di chiave e
        // valore
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (final KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            ProducerRecord<String, String> record = null;

            for (int counter = 0; counter < maxMessages; counter++) {
                String key = "K" + counter;
                String value = "Messagio nr " + counter;

                record = new ProducerRecord<>(topicName, key, value);
                String headerInfo = "MSG KEY: " + record.key();
                record.headers().add("headerKey", headerInfo.getBytes());

                try {

                    producer.send(
                            record,
                            new Callback() {
                                @Override
                                public void onCompletion(RecordMetadata metadata, Exception exception) {

                                    if (exception == null) {
                                        printMetadata(key, metadata);
                                    } else {
                                        System.out.println("Errore durante l'invio del messaggio: " + key);
                                    }

                                }
                            });

                    /* APPROCCIO CON LAMBDA (FUNZIONALE)
                    producer.send(
                            record,
                            (metadata, exception) -> {
                                if (exception == null) {
                                    printMetadata(key, metadata);
                                } else {
                                    System.out.println("Errore durante l'invio del messaggio: " + key);
                                }
                            });
                    */
                    // printMetadata(key, metadata);

                } catch (Exception ex) {
                    System.out.println("Errore durante l'invio del messaggio: " + counter);
                }

            }

            producer.flush();
            producer.close();

        }

    }

}
