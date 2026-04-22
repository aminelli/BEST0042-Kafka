-- Script di inizializzazione del database MySQL per il POC E-commerce
-- Questo script crea le tabelle necessarie per simulare un sistema di e-commerce

-- Creazione database (se non esiste già)
CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

-- Tabella CUSTOMERS: contiene i dati dei clienti
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    city VARCHAR(100),
    country VARCHAR(100),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabella PRODUCTS: catalogo prodotti
CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabella ORDERS: ordini effettuati dai clienti
CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    shipping_address TEXT,
    payment_method VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_order_date (order_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabella ORDER_ITEMS: dettaglio articoli per ogni ordine
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inserimento dati di esempio per CUSTOMERS
INSERT INTO customers (first_name, last_name, email, phone, city, country) VALUES
('Mario', 'Rossi', 'mario.rossi@email.com', '+39 333 1234567', 'Milano', 'Italy'),
('Laura', 'Bianchi', 'laura.bianchi@email.com', '+39 333 2345678', 'Roma', 'Italy'),
('Giuseppe', 'Verdi', 'giuseppe.verdi@email.com', '+39 333 3456789', 'Napoli', 'Italy'),
('Anna', 'Neri', 'anna.neri@email.com', '+39 333 4567890', 'Torino', 'Italy'),
('Francesco', 'Gialli', 'francesco.gialli@email.com', '+39 333 5678901', 'Firenze', 'Italy'),
('Elena', 'Ferrari', 'elena.ferrari@email.com', '+39 333 6789012', 'Bologna', 'Italy'),
('Luca', 'Colombo', 'luca.colombo@email.com', '+39 333 7890123', 'Venezia', 'Italy'),
('Sara', 'Ricci', 'sara.ricci@email.com', '+39 333 8901234', 'Palermo', 'Italy'),
('Marco', 'Moretti', 'marco.moretti@email.com', '+39 333 9012345', 'Genova', 'Italy'),
('Giulia', 'Conti', 'giulia.conti@email.com', '+39 333 0123456', 'Verona', 'Italy');

-- Inserimento dati di esempio per PRODUCTS
INSERT INTO products (product_name, category, price, stock_quantity, description) VALUES
('Laptop Dell XPS 15', 'Electronics', 1299.99, 50, 'High-performance laptop for professionals'),
('iPhone 14 Pro', 'Electronics', 1099.99, 100, 'Latest Apple smartphone'),
('Samsung 4K TV 55"', 'Electronics', 699.99, 30, 'Ultra HD Smart TV'),
('Nike Air Max Sneakers', 'Fashion', 129.99, 200, 'Comfortable running shoes'),
('Levi''s Jeans 501', 'Fashion', 89.99, 150, 'Classic denim jeans'),
('Coffee Machine Delonghi', 'Home', 299.99, 75, 'Automatic espresso machine'),
('Dyson Vacuum Cleaner', 'Home', 399.99, 40, 'Cordless vacuum cleaner'),
('The Lord of the Rings Book Set', 'Books', 49.99, 120, 'Complete trilogy box set'),
('Yoga Mat Premium', 'Sports', 39.99, 180, 'Non-slip exercise mat'),
('PlayStation 5', 'Electronics', 499.99, 25, 'Next-gen gaming console'),
('Adidas Running Shoes', 'Fashion', 99.99, 160, 'Lightweight running shoes'),
('Kitchen Knife Set', 'Home', 79.99, 90, 'Professional chef knives'),
('Harry Potter Complete Collection', 'Books', 89.99, 85, 'All 7 books hardcover'),
('Fitness Tracker Band', 'Sports', 69.99, 140, 'Smart fitness band with heart monitor'),
('Wireless Headphones Sony', 'Electronics', 249.99, 110, 'Noise-cancelling headphones');

-- Inserimento dati di esempio per ORDERS
INSERT INTO orders (customer_id, total_amount, status, shipping_address, payment_method) VALUES
(1, 1429.98, 'DELIVERED', 'Via Roma 123, Milano, 20100', 'CREDIT_CARD'),
(2, 699.99, 'SHIPPED', 'Via Veneto 45, Roma, 00100', 'PAYPAL'),
(3, 219.98, 'PENDING', 'Via Napoli 67, Napoli, 80100', 'CREDIT_CARD'),
(4, 1099.99, 'PROCESSING', 'Corso Francia 89, Torino, 10100', 'CREDIT_CARD'),
(5, 339.97, 'DELIVERED', 'Via Firenze 12, Firenze, 50100', 'DEBIT_CARD');

-- Inserimento dati di esempio per ORDER_ITEMS
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1, 1, 1299.99, 1299.99),
(1, 4, 1, 129.99, 129.99),
(2, 3, 1, 699.99, 699.99),
(3, 5, 2, 89.99, 179.98),
(3, 9, 1, 39.99, 39.99),
(4, 2, 1, 1099.99, 1099.99),
(5, 6, 1, 299.99, 299.99),
(5, 9, 1, 39.99, 39.99);

-- =====================================================
-- CONFIGURAZIONE PRIVILEGI PER CDC (DEBEZIUM)
-- =====================================================
-- L'utente 'ecommerce_user' viene creato automaticamente da MySQL
-- tramite le variabili d'ambiente MYSQL_USER e MYSQL_PASSWORD.
-- Qui aggiungiamo i privilegi necessari per Change Data Capture:

-- Privilegi per operazioni di snapshot e flush delle tabelle
GRANT RELOAD, FLUSH_TABLES ON *.* TO 'ecommerce_user'@'%';

-- Privilegi per leggere il binlog e replicare i dati
GRANT REPLICATION CLIENT, REPLICATION SLAVE ON *.* TO 'ecommerce_user'@'%';

-- Privilegi standard sul database ecommerce_db
GRANT SELECT, INSERT, UPDATE, DELETE ON ecommerce_db.* TO 'ecommerce_user'@'%';

-- Applica le modifiche
FLUSH PRIVILEGES;

-- Visualizzazione riassunto dati inseriti
SELECT 'Database initialized successfully!' AS status;
SELECT COUNT(*) AS total_customers FROM customers;
SELECT COUNT(*) AS total_products FROM products;
SELECT COUNT(*) AS total_orders FROM orders;
SELECT COUNT(*) AS total_order_items FROM order_items;

-- Verifica privilegi utente CDC
SELECT 'CDC privileges granted successfully!' AS cdc_status;
SHOW GRANTS FOR 'ecommerce_user'@'%';
