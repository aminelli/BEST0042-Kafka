#!/bin/bash

# Network Monitoring System - Setup Script

set -e

echo "🚀 Network Monitoring System - Setup"
echo "===================================="

# Check prerequisites
echo ""
echo "📋 Checking prerequisites..."

command -v docker >/dev/null 2>&1 || { echo "❌ Docker is required but not installed. Aborting."; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo "❌ Docker Compose is required but not installed. Aborting."; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "❌ Maven is required but not installed. Aborting."; exit 1; }

echo "✅ Docker found: $(docker --version)"
echo "✅ Docker Compose found: $(docker-compose --version)"
echo "✅ Maven found: $(mvn --version | head -n 1)"

# Build the project
echo ""
echo "🔨 Building the project with Maven..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

# Create necessary directories
echo ""
echo "📁 Creating necessary directories..."
mkdir -p logs checkpoint

echo "✅ Directories created!"

# Pull Docker images
echo ""
echo "🐳 Pulling Docker images..."
docker-compose pull

# Start infrastructure services
echo ""
echo "🚀 Starting infrastructure services (Kafka, Elasticsearch, Kibana)..."
docker-compose up -d zookeeper kafka elasticsearch kibana

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check Kafka
echo "Checking Kafka..."
timeout 60 bash -c 'until docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 2>/dev/null; do sleep 2; done'
echo "✅ Kafka is ready!"

# Check Elasticsearch
echo "Checking Elasticsearch..."
timeout 60 bash -c 'until curl -s http://localhost:9200/_cluster/health | grep -q "yellow\|green"; do sleep 2; done'
echo "✅ Elasticsearch is ready!"

# Create Kafka topics
echo ""
echo "📝 Creating Kafka topics..."
docker exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 \
  --topic network-telemetry-type-a --partitions 3 --replication-factor 1

docker exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 \
  --topic network-telemetry-type-b --partitions 3 --replication-factor 1

docker exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 \
  --topic network-telemetry-normalized --partitions 3 --replication-factor 1

echo "✅ Kafka topics created!"

# Create Elasticsearch index template
echo ""
echo "📊 Creating Elasticsearch index template..."
curl -X PUT "localhost:9200/_index_template/network-telemetry-template" \
  -H 'Content-Type: application/json' -d'
{
  "index_patterns": ["network-telemetry-*"],
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "index.lifecycle.name": "network-telemetry-policy"
    },
    "mappings": {
      "properties": {
        "normalizedId": { "type": "keyword" },
        "originalDeviceId": { "type": "keyword" },
        "deviceType": { "type": "keyword" },
        "timestamp": { "type": "date" },
        "location": { "type": "keyword" },
        "processingTimestamp": { "type": "date" },
        "version": { "type": "keyword" },
        "metrics": {
          "properties": {
            "totalBytesTransferred": { "type": "long" },
            "totalPackets": { "type": "long" },
            "errorRate": { "type": "double" },
            "utilization": { "type": "double" },
            "activeConnections": { "type": "integer" }
          }
        }
      }
    }
  }
}'

echo ""
echo "✅ Elasticsearch index template created!"

# Start application services
echo ""
echo "🚀 Starting application services..."
docker-compose up -d

echo ""
echo "✅ All services started!"

echo ""
echo "===================================="
echo "✅ Setup completed successfully!"
echo "===================================="
echo ""
echo "📊 Access Points:"
echo "  - Kibana: http://localhost:5601"
echo "  - Elasticsearch: http://localhost:9200"
echo ""
echo "📝 Useful commands:"
echo "  - View logs: docker-compose logs -f"
echo "  - Stop all: docker-compose down"
echo "  - Restart: docker-compose restart"
echo ""
echo "📖 Next steps:"
echo "  1. Open Kibana at http://localhost:5601"
echo "  2. Create index pattern: network-telemetry-*"
echo "  3. Go to Discover to view telemetry data"
echo ""
