FROM flink:1.18.1-java21

# Copia del JAR dell'applicazione
# Il JAR viene creato con 'mvn clean package'
COPY target/flink-ecommerce-processor-1.0.0.jar /opt/flink/usrlib/

# Configurazione Flink (opzionale, può essere sovrascritto dal docker-compose)
ENV FLINK_PROPERTIES="jobmanager.rpc.address: flink-jobmanager"
