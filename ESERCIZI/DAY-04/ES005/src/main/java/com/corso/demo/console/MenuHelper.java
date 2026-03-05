package com.corso.demo.console;

import com.corso.demo.producers.ProducerSyncAcksAll;
import com.corso.demo.producers.ProducerSyncAcksOne;
import com.corso.demo.producers.ProducerSyncFireAndForget;

public final class MenuHelper {

        public static void startMenu() {
                // Crea un'istanza del menu TUI con il titolo dell'applicazione
                MenuTUI menu = new MenuTUI("Kafka Course - Menu Principale");

                addMenuProducerFireAndForget(menu);
                addMenuProducerAcksOne(menu);
                addMenuProducerAcksAll(menu);

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