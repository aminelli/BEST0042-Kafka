FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY common ./common
COPY producer-type-a ./producer-type-a
RUN apk add --no-cache maven
RUN mvn clean package -pl producer-type-a -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/producer-type-a/target/router-producer.jar /app/router-producer.jar
RUN mkdir -p /app/config /app/logs
CMD ["java", "-jar", "/app/router-producer.jar", "/app/config/producer-type-a.properties"]
