package com.corso.demo.console;

public final class MenuHelper {

        public static void startMenu() {
                // Crea un'istanza del menu TUI con il titolo dell'applicazione
                MenuTUI menu = new MenuTUI("Java 21 Course - Menu Principale");

                // Aggiunge la voce per uscire dall'applicazione
                // Quando selezionata, questa voce termina il programma
                addMenuItemExit(menu);

                // Avvia il loop principale del menu
                // Il programma rimarrà in esecuzione finché l'utente non seleziona l'opzione di
                // uscita
                menu.start();
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