package com.corso.demo.console;

import com.corso.demo.consumers.ConsumerGeneric;
import com.corso.demo.producers.ProducerAsyncAcksAll;
import com.corso.demo.producers.ProducerCustomers;
import com.corso.demo.producers.ProducerCustomersJsonSer;
import com.corso.demo.producers.ProducerSyncAcksAll;
import com.corso.demo.producers.ProducerSyncAcksOne;
import com.corso.demo.producers.ProducerSyncFireAndForget;
import com.corso.demo.producers.ProducerWithPartitioner;
import com.corso.demo.streams.StreamDemo;

public final class MenuHelper {

    public static void startMenu() {
        // Crea un'istanza del menu TUI con il titolo dell'applicazione
        MenuTUI menu = new MenuTUI("Kafka Course - Menu Principale");

        addMenuProducerFireAndForget(menu);
        addMenuProducerAcksOne(menu);
        addMenuProducerAcksAll(menu);
        addMenuProducerAcksAllAsync(menu);
        addMenuProducerWithPartitioner(menu);
        addMenuProducerCustomers(menu);
        addMenuProducerCustomersJsonSer(menu);
        addMenuConsumerGeneric(menu);
        addMenuKafkaStreamDemo(menu);

        
        // Aggiunge la voce per uscire dall'applicazione
        // Quando selezionata, questa voce termina il programma
        addMenuItemExit(menu);

        // Avvia il loop principale del menu
        // Il programma rimarrà in esecuzione finché l'utente non seleziona l'opzione di
        // uscita
        menu.start();
    }

    private static void addMenuProducerFireAndForget(MenuTUI menu) {
        menu.addMenuItem("Producer Fire and Forget", () -> {
            // Crea un'istanza del producer e invia i messaggi
            ProducerSyncFireAndForget producer = new ProducerSyncFireAndForget();
            producer.sendMessages("demo-topic", 1000000);

            // Attende l'input dell'utente prima di tornare al menu
            System.out.println("\nPremi Invio per tornare al menu...");
            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignora eventuali errori di input
            }
        });
    }

    private static void addMenuProducerAcksOne(MenuTUI menu) {
        menu.addMenuItem("Producer Acks One", () -> {
            // Crea un'istanza del producer e invia i messaggi
            ProducerSyncAcksOne producer = new ProducerSyncAcksOne();
            producer.sendMessages("demo-topic-acks-1", 1000);

            // Attende l'input dell'utente prima di tornare al menu
            System.out.println("\nPremi Invio per tornare al menu...");
            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignora eventuali errori di input
            }
        });
    }

    private static void addMenuProducerAcksAll(MenuTUI menu) {
        menu.addMenuItem("Producer Acks All", () -> {
            // Crea un'istanza del producer e invia i messaggi
            ProducerSyncAcksAll producer = new ProducerSyncAcksAll();
            producer.sendMessages("demo-topic-corso", 1000);

            // Attende l'input dell'utente prima di tornare al menu
            System.out.println("\nPremi Invio per tornare al menu...");
            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignora eventuali errori di input
            }
        });
    }

    private static void addMenuProducerAcksAllAsync(MenuTUI menu) {
        menu.addMenuItem("Producer Acks All Async", () -> {
            // Crea un'istanza del producer e invia i messaggi
            ProducerAsyncAcksAll producer = new ProducerAsyncAcksAll();
            producer.sendMessages("demo-topic-async", 3000);

            // Attende l'input dell'utente prima di tornare al menu
            System.out.println("\nPremi Invio per tornare al menu...");
            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignora eventuali errori di input
            }
        });
    }

    private static void addMenuProducerWithPartitioner(MenuTUI menu) {
        menu.addMenuItem("Producer With Partitioner", () -> {
            // Crea un'istanza del producer e invia i messaggi
            ProducerWithPartitioner producer = new ProducerWithPartitioner();
            producer.sendMessages("demo-part", 3000);

            // Attende l'input dell'utente prima di tornare al menu
            System.out.println("\nPremi Invio per tornare al menu...");

            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignora eventuali errori di input
            }
        });
    }

    private static void addMenuProducerCustomers(MenuTUI menu) {
        menu.addMenuItem("Producer Customers", () -> {
            // Crea un'istanza del producer e invia i messaggi
            ProducerCustomers producer = new ProducerCustomers();
            producer.sendMessages("demo-customers", 10);
            producer.sendMessagesBytes("demo-customers-bytes", 10);
            // Attende l'input dell'utente prima di tornare al menu
            System.out.println("\nPremi Invio per tornare al menu...");

            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignora eventuali errori di input
            }
        });
    }

    private static void addMenuProducerCustomersJsonSer(MenuTUI menu) {
        menu.addMenuItem("Producer Customers JSON Ser", () -> {
            // Crea un'istanza del producer e invia i messaggi
            ProducerCustomersJsonSer producer = new ProducerCustomersJsonSer();
            producer.sendMessagesJsonCustomer("demo-customers-json", 10);
            producer.sendMessagesJsonGeneric("demo-generic-json", 10);

            // Attende l'input dell'utente prima di tornare al menu
            System.out.println("\nPremi Invio per tornare al menu...");

            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignora eventuali errori di input
            }
        });
    }

    private static void addMenuConsumerGeneric(MenuTUI menu) {
        menu.addMenuItem("Generic Consumer", () -> {
            // Crea un'istanza consumer e riceve i messaggi

            ConsumerGeneric consumer = new ConsumerGeneric();
            consumer.loadRecors(
                    "demo-part",
                    "generic-consumer-group",
                    "generic-consumer-1",
                    org.apache.kafka.common.serialization.StringDeserializer.class,
                    org.apache.kafka.common.serialization.StringDeserializer.class);

            // Attende l'input dell'utente prima di tornare al menu
            System.out.println("\nPremi Invio per tornare al menu...");

            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignora eventuali errori di input
            }
        });
    }

    private static void addMenuKafkaStreamDemo(MenuTUI menu) {
        menu.addMenuItem("Kafka Stream Demo", () -> {
            // Crea un'istanza consumer e riceve i messaggi

            StreamDemo demo = new StreamDemo();
            demo.runDemo();

            // Attende l'input dell'utente prima di tornare al menu
            System.out.println("\nPremi Invio per tornare al menu...");

            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignora eventuali errori di input
            }
        });
    }

    private static void addMenuItemExit(MenuTUI menu) {
        menu.addMenuItem("Esci dall'applicazione", () -> {
            // Pulisce lo schermo
            System.out.print("\033[H\033[2J");
            System.out.flush();

            // Definisce i colori
            String yellow = "\033[93m";
            String cyan = "\033[96m";
            String reset = "\033[0m";

            // Definisce i messaggi
            String msg1 = "Grazie per aver utilizzato Java 21 Course!";
            String msg2 = "Buono studio e buon coding! ";

            // Calcola il padding per centrare i messaggi (larghezza = 60)
            int width = 60;
            int padding1 = (width - msg1.length()) / 2;
            int padding2 = (width - msg2.length()) / 2;

            // Mostra un messaggio di commiato colorato con cornici allineate
            System.out.println("\n");
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                                                            ║");
            System.out.printf("║%s%s%s%s%s%s║%n",
                    " ".repeat(padding1),
                    yellow,
                    msg1,
                    reset,
                    " ".repeat(width - msg1.length() - padding1),
                    "");
            System.out.println("║                                                            ║");
            System.out.printf("║%s%s%s%s%s%s║%n",
                    " ".repeat(padding2),
                    cyan,
                    msg2,
                    reset,
                    " ".repeat(width - msg2.length() - padding2),
                    "");
            System.out.println("║                                                            ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");

            // Chiude le risorse del menu
            menu.close();

            // Termina il menu
            menu.stop();
        });
    }

}