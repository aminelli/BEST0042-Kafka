


```shell

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic traffic.metrics --from-beginning --bootstrap-server localhost:9092

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic traffic.alerts --from-beginning --bootstrap-server localhost:9092

```
