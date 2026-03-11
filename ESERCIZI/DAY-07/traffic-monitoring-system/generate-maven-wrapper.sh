#!/bin/bash

# Script to generate Maven Wrapper for all projects

set -e

echo "Generating Maven Wrapper for all projects..."

# Producer
echo "Generating wrapper for producer-tomtom..."
cd producer-tomtom
mvn wrapper:wrapper
chmod +x mvnw
cd ..

# Stream Processor
echo "Generating wrapper for stream-processor..."
cd stream-processor
mvn wrapper:wrapper
chmod +x mvnw
cd ..

# Consumer
echo "Generating wrapper for consumer-mongodb..."
cd consumer-mongodb
mvn wrapper:wrapper
chmod +x mvnw
cd ..

echo "Maven Wrapper generated successfully for all projects!"
