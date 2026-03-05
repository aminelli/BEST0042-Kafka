package com.corso.demo.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe che implementa un menu TUI (Text-based User Interface) elegante
 * con supporto per colori ANSI e decorazioni Unicode.
 */
public class MenuTUI {
    
    // Codici ANSI per i colori
    private static final String RESET = "\033[0m";
    private static final String CYAN = "\033[96m";
    private static final String GREEN = "\033[92m";
    private static final String YELLOW = "\033[93m";
    private static final String RED = "\033[91m";
    private static final String BOLD = "\033[1m";
    private static final String BLUE = "\033[94m";
    
    // Caratteri Unicode per le decorazioni
    private static final String TOP_LEFT = "╔";
    private static final String TOP_RIGHT = "╗";
    private static final String BOTTOM_LEFT = "╚";
    private static final String BOTTOM_RIGHT = "╝";
    private static final String HORIZONTAL = "═";
    private static final String VERTICAL = "║";
    private static final String T_RIGHT = "╠";
    private static final String T_LEFT = "╣";
    
    private final String title;
    private final List<MenuItem> menuItems;
    private final Scanner scanner;
    private boolean running;
    
    /**
     * Costruttore del menu TUI
     * @param title Il titolo da visualizzare nel menu
     */
    public MenuTUI(String title) {
        this.title = title;
        this.menuItems = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.running = false;
    }
    
    /**
     * Classe interna che rappresenta una voce del menu
     */
    public static class MenuItem {
        private final String description;
        private final Runnable action;
        
        public MenuItem(String description, Runnable action) {
            this.description = description;
            this.action = action;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void execute() {
            action.run();
        }
    }
    
    /**
     * Aggiunge una voce al menu
     * @param description La descrizione della voce
     * @param action L'azione da eseguire quando la voce viene selezionata
     */
    public void addMenuItem(String description, Runnable action) {
        menuItems.add(new MenuItem(description, action));
    }
    
    /**
     * Pulisce lo schermo (funziona su terminali che supportano codici ANSI)
     */
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    /**
     * Disegna una linea orizzontale decorativa
     * @param length La lunghezza della linea
     * @param start Il carattere iniziale
     * @param end Il carattere finale
     */
    private void drawHorizontalLine(int length, String start, String end) {
        System.out.print(CYAN + start);
        for (int i = 0; i < length; i++) {
            System.out.print(HORIZONTAL);
        }
        System.out.println(end + RESET);
    }
    
    /**
     * Disegna una riga con bordi verticali e contenuto centrato
     * @param content Il contenuto da visualizzare
     * @param length La lunghezza totale della riga
     */
    private void drawRowWithBorders(String content, int length) {
        // Calcola la lunghezza visibile rimuovendo i codici ANSI
        int visibleLength = getVisibleLength(content);
        int padding = length - visibleLength;
        int paddingLeft = padding / 2;
        int paddingRight = padding - paddingLeft;
        
        System.out.print(CYAN + VERTICAL + RESET);
        System.out.print(" ".repeat(paddingLeft));
        System.out.print(content);
        System.out.print(" ".repeat(paddingRight));
        System.out.println(CYAN + VERTICAL + RESET);
    }
    
    /**
     * Disegna una riga con bordi verticali e contenuto allineato a sinistra
     * @param content Il contenuto da visualizzare
     * @param length La lunghezza totale della riga
     */
    private void drawRowLeftAligned(String content, int length) {
        // Calcola la lunghezza visibile rimuovendo i codici ANSI
        int visibleLength = getVisibleLength(content);
        int padding = length - visibleLength;
        
        System.out.print(CYAN + VERTICAL + RESET);
        System.out.print(" ");
        System.out.print(content);
        System.out.print(" ".repeat(padding - 1));
        System.out.println(CYAN + VERTICAL + RESET);
    }
    
    /**
     * Calcola la lunghezza visibile di una stringa rimuovendo i codici ANSI
     * @param text Il testo da misurare
     * @return La lunghezza visibile del testo
     */
    private int getVisibleLength(String text) {
        // Rimuove tutti i codici ANSI dalla stringa
        String withoutAnsi = text.replaceAll("\\033\\[[0-9;]*m", "");
        return withoutAnsi.length();
    }
    
    /**
     * Visualizza il menu completo con tutte le decorazioni
     */
    private void displayMenu() {
        int width = 60;
        
        // Pulisce lo schermo prima di visualizzare il menu
        clearScreen();
        
        // Barra superiore
        drawHorizontalLine(width, TOP_LEFT, TOP_RIGHT);
        
        // Titolo
        String formattedTitle = BOLD + YELLOW + title + RESET;
        drawRowWithBorders(formattedTitle, width);
        
        // Separatore
        drawHorizontalLine(width, T_RIGHT, T_LEFT);
        
        // Voci del menu
        for (int i = 0; i < menuItems.size(); i++) {
            String item = String.format("%s[%d]%s %s", 
                GREEN, 
                i + 1, 
                RESET, 
                menuItems.get(i).getDescription());
            drawRowLeftAligned(item, width);
        }
        
        // Barra inferiore
        drawHorizontalLine(width, BOTTOM_LEFT, BOTTOM_RIGHT);
        
        // Prompt per l'input
        System.out.print("\n" + BLUE + "➤ Seleziona un'opzione: " + RESET);
    }
    
    /**
     * Gestisce l'input dell'utente e esegue l'azione corrispondente
     */
    private void handleInput() {
        try {
            String input = scanner.nextLine().trim();
            
            // Verifica se l'input è un numero valido
            int choice = Integer.parseInt(input);
            
            // Verifica se la scelta è nel range valido
            if (choice >= 1 && choice <= menuItems.size()) {
                // Esegue l'azione associata alla voce selezionata
                menuItems.get(choice - 1).execute();
            } else {
                showError("Opzione non valida. Premi INVIO per continuare...");
            }
        } catch (NumberFormatException e) {
            // L'input non è un numero
            // showError("Inserisci un numero valido. Premi INVIO per continuare...");
        }
    }
    
    /**
     * Mostra un messaggio di errore
     * @param message Il messaggio da visualizzare
     */
    private void showError(String message) {
        System.out.println("\n" + RED + "✖ " + message + RESET);
        scanner.nextLine();
    }
    
    /**
     * Mostra un messaggio di successo
     * @param message Il messaggio da visualizzare
     */
    public void showMessage(String message) {
        System.out.println("\n" + GREEN + "✔ " + message + RESET);
    }
    
    /**
     * Mette in pausa l'esecuzione in attesa di input dall'utente
     */
    public void waitForEnter() {
        System.out.print("\n" + YELLOW + "Premi INVIO per continuare..." + RESET);
        scanner.nextLine();
    }
    
    /**
     * Avvia il loop principale del menu
     */
    public void start() {
        running = true;
        
        // Loop principale che continua finché running è true
        while (running) {
            displayMenu();
            handleInput();
        }
    }
    
    /**
     * Termina l'esecuzione del menu
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Chiude le risorse utilizzate dal menu
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
