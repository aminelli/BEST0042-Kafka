# Java Console


---

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic demo-topic-acks-1 --from-beginning --bootstrap-server localhost:9092

---

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic demo-topic-acks-all --from-beginning --bootstrap-server localhost:9092

---

docker exec -it kafka01 /opt/kafka/bin/kafka-topics.sh --create --topic demo-topic-corso --bootstrap-server localhost:9092 --replication-factor 1 --partitions 6

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic demo-topic-corso --from-beginning --bootstrap-server localhost:9092

---

