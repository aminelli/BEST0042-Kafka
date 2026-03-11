.PHONY: help build up down logs clean kafka-topics mongo-shell

help:
	@echo "Traffic Monitoring System - Make Commands"
	@echo "==========================================="
	@echo "build          - Build all Java applications"
	@echo "docker-build   - Build Docker images"
	@echo "up             - Start all services with Docker Compose"
	@echo "down           - Stop all services"
	@echo "logs           - View logs from all services"
	@echo "clean          - Clean build artifacts and volumes"
	@echo "kafka-topics   - List Kafka topics"
	@echo "kafka-ui       - Open Kafka UI in browser"
	@echo "mongo-shell    - Open MongoDB shell"
	@echo "status         - Show status of all services"

build:
	@echo "Building Java applications..."
	@bash build-all.sh

docker-build:
	@echo "Building Docker images..."
	docker-compose build

up:
	@echo "Starting all services..."
	docker-compose up -d
	@echo "Services started. View logs with: make logs"

down:
	@echo "Stopping all services..."
	docker-compose down

logs:
	docker-compose logs -f

clean:
	@echo "Cleaning build artifacts and volumes..."
	docker-compose down -v
	cd producer-tomtom && ./mvnw clean || true
	cd stream-processor && ./mvnw clean || true
	cd consumer-mongodb && ./mvnw clean || true
	@echo "Clean complete"

kafka-topics:
	@echo "Listing Kafka topics..."
	docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

kafka-ui:
	@echo "Opening Kafka UI..."
	@which xdg-open > /dev/null && xdg-open http://localhost:8080 || open http://localhost:8080

mongo-shell:
	@echo "Opening MongoDB shell..."
	docker exec -it mongodb mongosh traffic_monitoring

status:
	@echo "Service Status:"
	@docker-compose ps
