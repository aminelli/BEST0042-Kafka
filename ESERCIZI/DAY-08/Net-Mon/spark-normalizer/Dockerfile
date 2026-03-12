FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY common ./common
COPY spark-normalizer ./spark-normalizer
RUN apk add --no-cache maven
RUN mvn clean package -pl spark-normalizer -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/spark-normalizer/target/spark-normalizer.jar /app/spark-normalizer.jar
RUN mkdir -p /app/config /app/logs /app/checkpoint /app/tmp
CMD ["java", \
    "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", \
    "--add-opens", "java.base/java.nio=ALL-UNNAMED", \
    "--add-opens", "java.base/java.util=ALL-UNNAMED", \
    "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
    "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED", \
    "--add-opens", "java.base/java.io=ALL-UNNAMED", \
    "-Dhadoop.home.dir=/app/tmp", \
    "-Djava.io.tmpdir=/app/tmp", \
    "-jar", "/app/spark-normalizer.jar", \
    "/app/config/spark-normalizer.properties"]
