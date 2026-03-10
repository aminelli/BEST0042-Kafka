


# Installazione confluent-hub in linux

```
wget -qO - https://packages.confluent.io/deb/8.2/archive.key | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://packages.confluent.io/deb/8.2 stable main"
sudo apt update
sudo apt install confluent-hub-client

confluent-hub install --component-dir confluent-hub-components --no-prompt debezium/debezium-connector-mysql:1.1.0
```
