FROM python:3.11-slim

# Impostazione working directory
WORKDIR /app

# Copia dei requirements e installazione dipendenze
# Questo layer viene cachato se requirements.txt non cambia
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia dello script generatore
COPY generator.py .

# Variabili d'ambiente di default (possono essere sovrascritte)
ENV MYSQL_HOST=mysql \
    MYSQL_PORT=3306 \
    MYSQL_USER=ecommerce_user \
    MYSQL_PASSWORD=ecommerce_pass \
    MYSQL_DATABASE=ecommerce_db \
    GENERATOR_BATCH_SIZE=10 \
    GENERATOR_INTERVAL_SECONDS=5 \
    GENERATOR_MAX_ORDERS=1000

# Comando di avvio
CMD ["python", "-u", "generator.py"]
