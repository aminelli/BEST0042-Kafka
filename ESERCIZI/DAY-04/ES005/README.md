# Java Console

---

docker run -d --hostname kafka01 --name kafka01 -p 9092:9092 --network net-kafka apache/kafka:4.2.0

docker run -d --hostname kafka-ui --name kafka-ui -p 9088:8080 -e DYNAMIC_CONFIG_ENABLED=true --network net-kafka provectuslabs/kafka-ui

---

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic demo-topic-acks-1 --from-beginning --bootstrap-server localhost:9092



---

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic demo-topic-acks-all --from-beginning --bootstrap-server localhost:9092

---

docker exec -it kafka01 /opt/kafka/bin/kafka-topics.sh --create --topic demo-topic-corso --bootstrap-server localhost:9092 --replication-factor 1 --partitions 6

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic demo-topic-corso --from-beginning --bootstrap-server localhost:9092

---


docker exec -it kafka01 /opt/kafka/bin/kafka-topics.sh --create --topic demo-part --bootstrap-server localhost:9092 --replication-factor 1 --partitions 4

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic demo-part --group gr-con-part --from-beginning --bootstrap-server localhost:9092 

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic demo-part --group gr-con-part-2 --from-beginning --bootstrap-server localhost:9092 

---

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic demo-customers --group gr-consumer-customers --from-beginning --bootstrap-server localhost:9092 


---