"""
Data Generator per E-commerce POC
Questo script genera continuamente dati casuali per simulare un sistema e-commerce reale.
Inserisce ordini, aggiorna prodotti e simula l'attività di un negozio online.
"""

import os
import sys
import time
import random
import logging
from datetime import datetime, timedelta
from typing import List, Tuple
import mysql.connector
from mysql.connector import Error
from faker import Faker
from dotenv import load_dotenv

# Caricamento variabili d'ambiente
load_dotenv()

# Configurazione logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

# Configurazione
MYSQL_HOST = os.getenv('MYSQL_HOST', 'mysql')
MYSQL_PORT = int(os.getenv('MYSQL_PORT', 3306))
MYSQL_USER = os.getenv('MYSQL_USER', 'ecommerce_user')
MYSQL_PASSWORD = os.getenv('MYSQL_PASSWORD', 'ecommerce_pass')
MYSQL_DATABASE = os.getenv('MYSQL_DATABASE', 'ecommerce_db')

BATCH_SIZE = int(os.getenv('GENERATOR_BATCH_SIZE', 10))
INTERVAL_SECONDS = int(os.getenv('GENERATOR_INTERVAL_SECONDS', 5))
MAX_ORDERS = int(os.getenv('GENERATOR_MAX_ORDERS', 1000))

# Inizializzazione Faker per dati realistici
fake = Faker('it_IT')

# Stati possibili per gli ordini
ORDER_STATUSES = ['PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED']
PAYMENT_METHODS = ['CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'BANK_TRANSFER']


class DatabaseConnection:
    """
    Gestisce la connessione al database MySQL con retry automatico.
    """
    
    def __init__(self, max_retries: int = 5, retry_delay: int = 5):
        self.max_retries = max_retries
        self.retry_delay = retry_delay
        self.connection = None
        self.connect()
    
    def connect(self):
        """
        Stabilisce la connessione al database con retry automatico.
        """
        for attempt in range(self.max_retries):
            try:
                logger.info(f"Tentativo di connessione al database MySQL ({attempt + 1}/{self.max_retries})...")
                self.connection = mysql.connector.connect(
                    host=MYSQL_HOST,
                    port=MYSQL_PORT,
                    user=MYSQL_USER,
                    password=MYSQL_PASSWORD,
                    database=MYSQL_DATABASE,
                    autocommit=False
                )
                
                if self.connection.is_connected():
                    db_info = self.connection.get_server_info()
                    logger.info(f"✓ Connesso a MySQL Server versione {db_info}")
                    return
                    
            except Error as e:
                logger.error(f"✗ Errore di connessione: {e}")
                if attempt < self.max_retries - 1:
                    logger.info(f"Nuovo tentativo tra {self.retry_delay} secondi...")
                    time.sleep(self.retry_delay)
                else:
                    logger.critical("Impossibile connettersi al database dopo tutti i tentativi")
                    raise
    
    def get_cursor(self):
        """
        Restituisce un cursore per eseguire query.
        """
        if not self.connection or not self.connection.is_connected():
            logger.warning("Connessione persa, riconnessione in corso...")
            self.connect()
        return self.connection.cursor()
    
    def commit(self):
        """
        Commit della transazione corrente.
        """
        if self.connection:
            self.connection.commit()
    
    def close(self):
        """
        Chiude la connessione al database.
        """
        if self.connection and self.connection.is_connected():
            self.connection.close()
            logger.info("Connessione al database chiusa")


class EcommerceDataGenerator:
    """
    Generatore di dati per il sistema e-commerce.
    Crea ordini, aggiorna prodotti e simula attività realistiche.
    """
    
    def __init__(self, db: DatabaseConnection):
        self.db = db
        self.customer_ids = []
        self.product_ids = []
        self.order_count = 0
        self._load_existing_data()
    
    def _load_existing_data(self):
        """
        Carica gli ID esistenti di clienti e prodotti dal database.
        """
        try:
            cursor = self.db.get_cursor()
            
            # Caricamento customer IDs
            cursor.execute("SELECT customer_id FROM customers")
            self.customer_ids = [row[0] for row in cursor.fetchall()]
            logger.info(f"Caricati {len(self.customer_ids)} clienti esistenti")
            
            # Caricamento product IDs con prezzi
            cursor.execute("SELECT product_id, price FROM products")
            self.product_ids = cursor.fetchall()
            logger.info(f"Caricati {len(self.product_ids)} prodotti esistenti")
            
            cursor.close()
            
        except Error as e:
            logger.error(f"Errore nel caricamento dati esistenti: {e}")
            raise
    
    def create_new_customer(self) -> int:
        """
        Crea un nuovo cliente nel database.
        Restituisce l'ID del cliente creato.
        """
        try:
            cursor = self.db.get_cursor()
            
            # Generazione dati cliente realistici
            first_name = fake.first_name()
            last_name = fake.last_name()
            email = f"{first_name.lower()}.{last_name.lower()}.{random.randint(1, 9999)}@email.com"
            phone = fake.phone_number()
            city = fake.city()
            country = 'Italy'
            
            query = """
                INSERT INTO customers (first_name, last_name, email, phone, city, country)
                VALUES (%s, %s, %s, %s, %s, %s)
            """
            values = (first_name, last_name, email, phone, city, country)
            
            cursor.execute(query, values)
            customer_id = cursor.lastrowid
            cursor.close()
            
            logger.info(f"✓ Nuovo cliente creato: {first_name} {last_name} (ID: {customer_id})")
            return customer_id
            
        except Error as e:
            logger.error(f"Errore nella creazione del cliente: {e}")
            raise
    
    def create_order(self, customer_id: int) -> Tuple[int, float]:
        """
        Crea un nuovo ordine per un cliente.
        Restituisce (order_id, total_amount).
        """
        try:
            cursor = self.db.get_cursor()
            
            # Selezione casuale di prodotti per l'ordine (1-5 prodotti)
            num_items = random.randint(1, 5)
            selected_products = random.sample(self.product_ids, min(num_items, len(self.product_ids)))
            
            # Calcolo totale ordine
            total_amount = 0
            order_items = []
            
            for product_id, price in selected_products:
                quantity = random.randint(1, 3)
                subtotal = price * quantity
                total_amount += subtotal
                order_items.append((product_id, quantity, price, subtotal))
            
            # Creazione ordine
            status = random.choice(ORDER_STATUSES)
            payment_method = random.choice(PAYMENT_METHODS)
            shipping_address = f"{fake.street_address()}, {fake.city()}, {fake.postcode()}"
            
            order_query = """
                INSERT INTO orders (customer_id, total_amount, status, shipping_address, payment_method)
                VALUES (%s, %s, %s, %s, %s)
            """
            cursor.execute(order_query, (customer_id, total_amount, status, shipping_address, payment_method))
            order_id = cursor.lastrowid
            
            # Inserimento items dell'ordine
            items_query = """
                INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
                VALUES (%s, %s, %s, %s, %s)
            """
            for product_id, quantity, unit_price, subtotal in order_items:
                cursor.execute(items_query, (order_id, product_id, quantity, unit_price, subtotal))
            
            cursor.close()
            
            logger.info(f"✓ Ordine creato: ID {order_id}, Cliente {customer_id}, "
                       f"Totale €{total_amount:.2f}, Items: {len(order_items)}, Status: {status}")
            
            return order_id, total_amount
            
        except Error as e:
            logger.error(f"Errore nella creazione dell'ordine: {e}")
            raise
    
    def update_order_status(self, order_id: int, new_status: str):
        """
        Aggiorna lo stato di un ordine esistente.
        Simula il flusso di processamento degli ordini.
        """
        try:
            cursor = self.db.get_cursor()
            
            query = "UPDATE orders SET status = %s, updated_at = CURRENT_TIMESTAMP WHERE order_id = %s"
            cursor.execute(query, (new_status, order_id))
            cursor.close()
            
            logger.info(f"✓ Ordine {order_id} aggiornato a stato: {new_status}")
            
        except Error as e:
            logger.error(f"Errore nell'aggiornamento dell'ordine: {e}")
            raise
    
    def update_product_stock(self, product_id: int, quantity_change: int):
        """
        Aggiorna la giacenza di un prodotto.
        Simula l'arrivo di nuova merce o vendite.
        """
        try:
            cursor = self.db.get_cursor()
            
            query = """
                UPDATE products 
                SET stock_quantity = GREATEST(0, stock_quantity + %s),
                    updated_at = CURRENT_TIMESTAMP
                WHERE product_id = %s
            """
            cursor.execute(query, (quantity_change, product_id))
            cursor.close()
            
            action = "incrementata" if quantity_change > 0 else "decrementata"
            logger.info(f"✓ Giacenza prodotto {product_id} {action} di {abs(quantity_change)} unità")
            
        except Error as e:
            logger.error(f"Errore nell'aggiornamento giacenza: {e}")
            raise
    
    def generate_batch(self):
        """
        Genera un batch di operazioni simulate:
        - Nuovi ordini
        - Aggiornamenti stato ordini
        - Aggiornamenti giacenze prodotti
        - Occasionalmente nuovi clienti
        """
        try:
            logger.info(f"=== Generazione batch #{self.order_count // BATCH_SIZE + 1} ===")
            
            for _ in range(BATCH_SIZE):
                # Con probabilità 10% crea un nuovo cliente
                if random.random() < 0.1:
                    new_customer_id = self.create_new_customer()
                    self.customer_ids.append(new_customer_id)
                
                # Creazione nuovo ordine
                if self.customer_ids:
                    customer_id = random.choice(self.customer_ids)
                    order_id, amount = self.create_order(customer_id)
                    self.order_count += 1
                
                # Con probabilità 30% aggiorna lo stato di un ordine precedente
                if random.random() < 0.3 and order_id > 10:
                    old_order_id = random.randint(1, order_id - 1)
                    new_status = random.choice(ORDER_STATUSES)
                    self.update_order_status(old_order_id, new_status)
                
                # Con probabilità 20% aggiorna la giacenza di un prodotto
                if random.random() < 0.2 and self.product_ids:
                    product_id, _ = random.choice(self.product_ids)
                    quantity_change = random.randint(-10, 50)
                    self.update_product_stock(product_id, quantity_change)
            
            # Commit di tutte le operazioni del batch
            self.db.commit()
            logger.info(f"✓ Batch completato e committato. Totale ordini generati: {self.order_count}")
            
        except Error as e:
            logger.error(f"Errore durante la generazione del batch: {e}")
            raise


def main():
    """
    Funzione principale che avvia il generatore di dati.
    """
    logger.info("=" * 80)
    logger.info("Avvio Data Generator per E-commerce POC")
    logger.info("=" * 80)
    logger.info(f"Configurazione:")
    logger.info(f"  - Database: {MYSQL_HOST}:{MYSQL_PORT}/{MYSQL_DATABASE}")
    logger.info(f"  - Batch size: {BATCH_SIZE} operazioni")
    logger.info(f"  - Intervallo: {INTERVAL_SECONDS} secondi")
    logger.info(f"  - Max ordini: {MAX_ORDERS}")
    logger.info("=" * 80)
    
    db = None
    
    try:
        # Connessione al database
        db = DatabaseConnection()
        
        # Inizializzazione generatore
        generator = EcommerceDataGenerator(db)
        
        # Loop principale di generazione
        while generator.order_count < MAX_ORDERS:
            generator.generate_batch()
            
            logger.info(f"Attesa {INTERVAL_SECONDS} secondi prima del prossimo batch...")
            logger.info(f"Progresso: {generator.order_count}/{MAX_ORDERS} ordini generati\n")
            
            time.sleep(INTERVAL_SECONDS)
        
        logger.info("=" * 80)
        logger.info(f"✓ Generazione completata! Totale ordini creati: {generator.order_count}")
        logger.info("=" * 80)
        
    except KeyboardInterrupt:
        logger.info("\n⚠ Interruzione ricevuta dall'utente")
        
    except Exception as e:
        logger.error(f"✗ Errore fatale: {e}", exc_info=True)
        sys.exit(1)
        
    finally:
        if db:
            db.close()
        logger.info("Data Generator terminato")


if __name__ == "__main__":
    main()
