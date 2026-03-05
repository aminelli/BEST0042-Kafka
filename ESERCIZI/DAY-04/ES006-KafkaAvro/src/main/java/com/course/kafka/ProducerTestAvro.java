package com.course.kafka;

import com.course.kafka.model.avro.Payment;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import net.datafaker.Faker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerTestAvro {

    private static final String TOPIC = "TEST_AVRO";
    private static final Properties props = new Properties();

    // @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws Exception {

        Faker faker = new Faker();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        try (KafkaProducer<String, Payment> producer = new KafkaProducer<String, Payment>(props)) {

            for (long count = 0; count < 1000; count++) {
                final Integer id = faker.idNumber().hashCode();
                final Double amount = faker.number().numberBetween(100d, 10000d);
                final String region = faker.address().state();

                //final Payment payment = new Payment(id.toString(), amount,region);
                final Payment payment = new Payment(id.toString(), amount, region);
                final ProducerRecord<String, Payment> record = new ProducerRecord<>(TOPIC, id.toString(), payment);
                producer.send(record);
                Thread.sleep(10L);

            }

            producer.flush();
            producer.close();

        } catch (final SerializationException ex) {
            ex.printStackTrace();
        } catch (final InterruptException ex) {
            ex.printStackTrace();
        }

    }

}
