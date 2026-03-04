# Esercizio


## Task



1. Effettuare il download dell'ultima versione di kafka
   1. https://www.apache.org/dyn/closer.cgi?path=/kafka/4.2.0/kafka_2.13-4.2.0.tgz
2. Scompattare
3. Aprire un prompt dei comandi
4. Posizionari tramite prompt nella folder di scompattamento 
5. Lanciando un comando dir dovrei dovrei poter vedere cartella bin, lib etc.. (passo facoltativo per veridicare se sono effettivamente nella cartella corretta)
6. Lanciare i comandi bash previsti nella sezione **Comandi Bash**



## Comandi bash


```batch
REM Aprire un nuovo prompt dei comandi,
REM Posizionarsi sulla folder principale di Kafka
REM (Ovvero la folder dove si è scompattati il file zip di kafka)
REM e lanciare il comando:
bin\windows\kafka-storage.bat format --standalone -t 12345 -c config\kraft\server.properties

REM Lanciare ora il comando per far partire il server:
bin\windows\kafka-server-start.bat config\kraft\server.properties  
 
REM Aprire un nuovo prompt dei comandi,
REM Posizionarsi sulla folder principale di Kafka
REM (Ovvero la folder dove si è scompattati il file zip di kafka)
REM e lanciare il comando per creare il topic 
bin\windows\kafka-topics.bat --create --topic quickstart-events --bootstrap-server localhost:9092

REM Lanciare il comando per verificare la creazione del topic 
bin\windows\kafka-topics.bat --describe --topic quickstart-events --bootstrap-server localhost:9092

REM Lanciare il comando per avviare un producer 
bin\windows\kafka-console-producer.bat --topic quickstart-events --bootstrap-server localhost:9092

REM Aprire un nuovo prompt dei comandi,
REM Posizionarsi sulla folder principale di Kafka
REM (Ovvero la folder dove si è scompattati il file zip di kafka)
REM e lanciare il comando pre creare il consumer:
bin\windows\kafka-console-consumer.bat --topic quickstart-events --from-beginning --bootstrap-server localhost:9092
``` 