#!/bin/bash

# Build script for Traffic Monitoring System
# This script builds all Java applications

set -e

echo "========================================="
echo "Building Traffic Monitoring System"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Build Producer
echo -e "\n${YELLOW}Building Producer TomTom...${NC}"
cd producer-tomtom
./mvnw clean package -DskipTests
echo -e "${GREEN}✓ Producer built successfully${NC}"
cd ..

# Build Stream Processor
echo -e "\n${YELLOW}Building Stream Processor...${NC}"
cd stream-processor
./mvnw clean package -DskipTests
echo -e "${GREEN}✓ Stream Processor built successfully${NC}"
cd ..

# Build Consumer
echo -e "\n${YELLOW}Building MongoDB Consumer...${NC}"
cd consumer-mongodb
./mvnw clean package -DskipTests
echo -e "${GREEN}✓ Consumer built successfully${NC}"
cd ..

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}All applications built successfully!${NC}"
echo -e "${GREEN}=========================================${NC}"

echo -e "\nNext steps:"
echo "1. Configure your TomTom API key in .env file"
echo "2. Run: docker-compose build"
echo "3. Run: docker-compose up -d"
