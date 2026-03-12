```shell

docker exec -it kafka01 /opt/kafka/bin/kafka-console-consumer.sh --topic network-telemetry-normalized --from-beginning --bootstrap-server localhost:9092

```