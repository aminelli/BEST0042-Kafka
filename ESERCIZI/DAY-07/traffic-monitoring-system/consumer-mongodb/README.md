

```shell


# docker pull mongodb/mongodb-community-server:7.0-ubi8


docker run -d --name mongodb --hostname mongodb --network net-kafka -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=root mongo


```shell

