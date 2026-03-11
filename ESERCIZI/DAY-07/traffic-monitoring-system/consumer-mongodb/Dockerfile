# Multi-stage Dockerfile for MongoDB Consumer
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml
COPY pom.xml ./

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR from builder stage
COPY --from=builder /app/target/consumer-mongodb-1.0.0.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD pgrep -f "consumer-mongodb" || exit 1

# Environment variables (overridable)
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092
ENV MONGODB_URI=mongodb://mongodb:27017
ENV MONGODB_DATABASE=traffic_monitoring
ENV CONSUMER_GROUP_ID=mongodb-consumer-group
ENV KAFKA_TOPIC_METRICS=traffic.metrics
ENV KAFKA_TOPIC_ALERTS=traffic.alerts

# Run application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
