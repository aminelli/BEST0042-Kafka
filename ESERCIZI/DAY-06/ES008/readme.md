
- https://docs.confluent.io/platform/current/ksqldb/tutorials/materialized-view.html#start-the-stack


# Installazione confluent-hub in linux

```sh
wget -qO - https://packages.confluent.io/deb/8.2/archive.key | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://packages.confluent.io/deb/8.2 stable main"
sudo apt update
sudo apt install confluent-hub-client

confluent-hub install --component-dir confluent-hub-components --no-prompt debezium/debezium-connector-mysql:1.1.0
```

# Sequenza comandi

```sh
docker compose up -d
docker exec -ir connect confluent-hub install --component-dir /usr/share/confluent-hub-components debezium/debezium-connector-mysql:latest
docker exec -it mysql /bin/bash
mysql -u root -p

```


```sql
-- SCRIPT MYSQL
GRANT ALL PRIVILEGES ON *.* TO 'example-user' WITH GRANT OPTION;

ALTER USER 'example-user'@'%' IDENTIFIED WITH mysql_native_password BY 'example-pw';
FLUSH PRIVILEGES;

USE call-center;

CREATE TABLE calls (name TEXT, reason TEXT, duration_seconds INT);

INSERT INTO calls (name, reason, duration_seconds) VALUES ("michael", "purchase", 540);
INSERT INTO calls (name, reason, duration_seconds) VALUES ("michael", "help", 224);
INSERT INTO calls (name, reason, duration_seconds) VALUES ("colin", "help", 802);
INSERT INTO calls (name, reason, duration_seconds) VALUES ("derek", "purchase", 10204);
INSERT INTO calls (name, reason, duration_seconds) VALUES ("derek", "help", 600);
INSERT INTO calls (name, reason, duration_seconds) VALUES ("colin", "refund", 105);
INSERT INTO calls (name, reason, duration_seconds) VALUES ("michael", "help", 2030);
INSERT INTO calls (name, reason, duration_seconds) VALUES ("colin", "purchase", 800);
INSERT INTO calls (name, reason, duration_seconds) VALUES ("derek", "help", 2514);
INSERT INTO calls (name, reason, duration_seconds) VALUES ("derek", "refund", 325);
```

