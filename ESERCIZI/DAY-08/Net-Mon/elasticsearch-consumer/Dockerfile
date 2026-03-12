FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY common ./common
COPY elasticsearch-consumer ./elasticsearch-consumer
RUN apk add --no-cache maven
RUN mvn clean package -pl elasticsearch-consumer -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/elasticsearch-consumer/target/elasticsearch-consumer.jar /app/elasticsearch-consumer.jar
RUN mkdir -p /app/config /app/logs
CMD ["java", "-jar", "/app/elasticsearch-consumer.jar", "/app/config/elasticsearch-consumer.properties"]
