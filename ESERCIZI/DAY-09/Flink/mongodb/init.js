// =====================================================
// Script di inizializzazione MongoDB
// =====================================================
// Crea il database e le collezioni necessarie con indici ottimizzati
//
// NOTA PRIVILEGI:
// L'utente 'admin' con password 'adminpassword' viene creato automaticamente
// tramite le variabili d'ambiente MONGO_INITDB_ROOT_USERNAME e MONGO_INITDB_ROOT_PASSWORD.
// Questo utente ha privilegi di root su tutti i database.
//
// Il Flink job si connette usando:
// mongodb://admin:adminpassword@mongodb:27017
// e ha accesso completo in lettura/scrittura su ecommerce_analytics
// =====================================================

// Selezione database
db = db.getSiblingDB('ecommerce_analytics');

// Creazione collezione per le aggregazioni degli ordini
db.createCollection('order_aggregations');

// Creazione indici per query ottimizzate
// Indice sulla finestra temporale per query temporali veloci
db.order_aggregations.createIndex({ "window_start": -1, "window_end": -1 });

// Indice sul timestamp di creazione
db.order_aggregations.createIndex({ "created_at": -1 });

// Indice sul revenue totale per query analitiche
db.order_aggregations.createIndex({ "total_revenue": -1 });

// Verifica creazione collezione
if (db.order_aggregations.exists()) {
    print('✓ Database ecommerce_analytics inizializzato');
    print('✓ Collezione order_aggregations creata');
    print('✓ Indici creati con successo:');
    
    // Stampa gli indici creati
    db.order_aggregations.getIndexes().forEach(function(index) {
        print('  - ' + index.name);
    });
    
    print('✓ Configurazione privilegi: utente root \'admin\' ha accesso completo');
} else {
    print('✗ Errore: impossibile creare la collezione order_aggregations');
}
