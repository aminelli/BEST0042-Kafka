package com.course.kafka;

import com.course.kafka.model.avro.Payment;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerTestAvro {

    private static final String TOPIC = "TEST_AVRO";
    private static final Properties props = new Properties();

    public static void main(String[] args) {

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");


        props.put(ConsumerConfig.GROUP_ID_CONFIG, "GR-PAYMENTS");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "APP-PAYMENTS-01");

        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);


        try (KafkaConsumer<String, Payment> consumer = new KafkaConsumer<>(props)) {

            consumer.subscribe(Collections.singletonList(TOPIC));

            consumer.seekToBeginning(consumer.assignment());


            while (true) {
               final ConsumerRecords<String, Payment> records = consumer.poll(Duration.ofMillis(100));
               for (ConsumerRecord<String, Payment> record : records) {
                   final String key = record.key();
                   final Payment payment = record.value();
                   System.out.printf("key %s -> payment = %s %n", key, payment);
               }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }



    }


}
