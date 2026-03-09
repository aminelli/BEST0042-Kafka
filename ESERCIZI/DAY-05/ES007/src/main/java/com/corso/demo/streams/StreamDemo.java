package com.corso.demo.streams;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import com.corso.demo.models.Customer;

import net.datafaker.Faker;

public class StreamDemo {

    private static final String TOPIC_SOURCE = "TOP-STREAM-SOURCE";
    private static final String TOPIC_DEST = "TOP-STREAM-DEST";

    private static final String APP_ID = "STREAM-DEMO-APP";


    private final AtomicBoolean running = new AtomicBoolean(true);

    private final CountDownLatch shutdownLatch = new CountDownLatch(3);

    // Contatori per statistiche
    private final AtomicInteger producedCount = new AtomicInteger(0);
    private final AtomicInteger consumedCount = new AtomicInteger(0);
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public void runDemo() {
        try {

            Thread producerThread = new Thread(this::runProducer, "Producer-Thread");
            producerThread.start();
            Thread.sleep(2000);

            Thread consumerThread = new Thread(this::runConsumer, "Consumer-Thread");
            consumerThread.start();
            Thread.sleep(2000);

            Thread streamThread = new Thread(this::runStreamProcessor, "Stream-Thread");
            streamThread.start();
            Thread.sleep(2000);

            System.out.println("=".repeat(50));
            System.out.println("Stream Demo avviata con successo!");
            System.out.println("=".repeat(50));

            // Attende che i thread completino l'esecuzione (opzionale, a seconda della
            // logica dei thread)
            Thread.sleep(60000);

            running.set(false); // Segnala ai thread di terminare
            shutdownLatch.await();

        } catch (InterruptedException ex) {
            System.out.println("Errore durante l'esecuzione della demo: " + ex.getMessage());
            running.set(false); // Segnala ai thread di terminare
            Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
        } catch (Exception ex) {
            System.out.println("Errore durante l'esecuzione della demo: " + ex.getMessage());
        }
    }

    private void runProducer() {

        Faker faker = new Faker();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "ProducerAsyncX");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Ritardo per batch
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16 * 1024); // Dimensione batch (16 KB)

        KafkaProducer<String, String> producer = null;

        try {
            producer = new KafkaProducer<>(props);
            Customer customer = null;
            ProducerRecord<String, String> record = null;

            while (running.get()) {
                String id = faker.idNumber().valid();
                String key = "K-" + id;

                customer = new Customer(
                        id,
                        faker.name().firstName(),
                        faker.name().lastName(),
                        faker.internet().emailAddress(),
                        faker.number().numberBetween(1, 80)
                    );

                String value = customer.toString();

                record = new ProducerRecord<>(TOPIC_SOURCE, key, value);
                String headerInfo = "MSG KEY: " + record.key();
                record.headers().add("headerKey", headerInfo.getBytes());

                producer.send(
                        record,
                        (metadata, exception) -> {
                            if (exception == null) {
                                int count = producedCount.incrementAndGet();
                                System.out.println("Messaggio inviato con successo: " + key + " | Totale prodotti: " + count);
                            } else {
                                System.out.println("Errore durante l'invio del messaggio: " + key);
                            }
                        });

                Thread.sleep(10); // Simula un ritardo tra i messaggi
            }

            System.out.println("Producer in fase di chiusura...");

        } catch (InterruptedException ex) {
            System.out.println("Errore durante l'esecuzione del producer: " + ex.getMessage());
            Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
        } catch (Exception ex) {
            System.out.println("Errore durante l'esecuzione del producer: " + ex.getMessage());
        } finally {
            if (producer != null) {
                producer.flush();
                producer.close();
            }
            shutdownLatch.countDown();
        }

    }

    private void runConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "Consumer Stream Read");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "CL001");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"); // Abilita auto-commit
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000"); // Intervallo di auto-commit

        Consumer<String, String> consumer = null; // new KafkaConsumer<>(props); 
        ConsumerRecords<String, String> records = null;
        
        try {

            consumer = new KafkaConsumer<>(props);

            consumer.subscribe(Collections.singletonList(TOPIC_DEST));

            while (running.get()) {
                
                int consumed = consumedCount.incrementAndGet();

                records = consumer.poll(java.time.Duration.ofMillis(500));
                
                records = consumer.poll(Duration.ofMillis(100));

                records.forEach(record -> {
                    String key = record.key();
                    String value = record.value();

                    // Qui mettiamola logica di business per processare i record

                    System.out.printf(
                        "Topic: %s, Partition: %d, Offset: %d, key; %s, value: %s, Totale consumati: %d \n", 
                        "TOP-STREAM-DEST",
                        record.partition(), 
                        record.offset(),
                        key,
                        value,
                        consumed
                    );
                    
                    
                });

            }

            System.out.println("Consumer in fase di chiusura...");

        } catch (Exception e) {
            if (running.get()) {
                System.out.println("Errore durante il consumo dei record: " + e.getMessage());
            } else {
                System.out.println("Consumer interrotto.");
            }
            
        } finally {
            if (consumer != null) {
                consumer.close();
            }
            shutdownLatch.countDown();
        }

    }

    private void runStreamProcessor() {

        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID);
       
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        
        // Commit
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500); // Intervallo di commit

        // Directory per state store
        props.put(StreamsConfig.STATE_DIR_CONFIG, "kafka-streams-state");

        // Cache per ottimizzazioni quando si fa aggregation
        //cache.max.bytes.buffering
        //props.put(StreamsConfig.bufferu, props)

        KafkaStreams streams = null;

        try {
                
          
            // Creo la tipologia di stream
            StreamsBuilder builder = new StreamsBuilder();

            // Creazione Kstream 
            // Kstream è un flusso di dati che rappresenta una sequenza di record, 
            // dove ogni record è composto da una chiave e un valore.
            KStream<String, String> inputStream = builder.stream(TOPIC_SOURCE);

            // PIPELINE DI TTRASFORMAZIONE
            inputStream
                // 1. Deserializzazione del valore da JSON a oggetto Customer 
                .mapValues( json -> {
                    try {
                        Customer customer = Customer.fromJson(json);
                        return customer;
                    } catch (Exception ex) {
                        System.out.println("Errore durante la deserializzazione del record: " + ex.getMessage());
                        return null; // O gestisci in altro modo i record non validi
                    }
                })
                // 2. Filtra solo per customer validi
                .filter((key, customer) -> customer != null)
                // 3. Filtra solo per customer maggiorenni
                .filter((key, customer) -> {
                    if (customer instanceof Customer) {
                        Boolean isValid = customer.getAge() >= 18;
                        if (isValid) {
                            processedCount.incrementAndGet();
                        }
                        return isValid; // Filtra solo per customer maggiorenni   
                    }
                    return false;                   
                })
                // 4. Trasformazione del valore in un nuovo formato (es. solo nome e email)
                .mapValues(customer -> {
                    return customer.toJson();
                })
                // 5. Invia il risultato al topic di destinazione
                .to(TOPIC_DEST)
                ;

                // Creazione Sream 
                streams = new KafkaStreams(builder.build(), props);

                // Gestione dello stato dello stream
                final KafkaStreams finalStreams = streams;
                streams.setStateListener((newState, oldState) -> {
                    System.out.println("Stream state changed da " + oldState + " a " + newState);                   
                });

                // Avvia io stream processor
                streams.start();

                while (running.get()) {
                    // Qui puoi aggiungere logica per monitorare lo stato dello stream o altre operazioni
                    Thread.sleep(1000);
                    int processed = processedCount.get();
                    System.out.println("Stream processor in esecuzione... Totale record processati: " + processed);
                }
                
                System.out.println("Stream processor in fase di chiusura...");


        } catch (InterruptedException ex) {
            System.out.println("Errore durante l'esecuzione dello stream processor: " + ex);
            Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione            
        } catch (Exception ex) {
            System.out.println("Errore durante l'esecuzione dello stream processor: " + ex.getMessage());
        } finally {
            // Qui va la logica per chiudere lo stream processor
            if (streams != null) {
                streams.close(Duration.ofSeconds(10));
            }
            shutdownLatch.countDown();
        }



    }

}
