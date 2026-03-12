FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY common ./common
COPY producer-type-b ./producer-type-b
RUN apk add --no-cache maven
RUN mvn clean package -pl producer-type-b -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/producer-type-b/target/switch-producer.jar /app/switch-producer.jar
RUN mkdir -p /app/config /app/logs
CMD ["java", "-jar", "/app/switch-producer.jar", "/app/config/producer-type-b.properties"]
