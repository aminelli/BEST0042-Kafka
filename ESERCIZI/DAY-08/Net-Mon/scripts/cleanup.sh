#!/bin/bash

# Cleanup script for Network Monitoring System

echo "🧹 Cleaning up Network Monitoring System..."

# Stop and remove containers
echo "Stopping containers..."
docker-compose down -v

# Remove logs and checkpoints
echo "Removing logs and checkpoints..."
rm -rf logs/* checkpoint/*

# Clean build artifacts
echo "Cleaning Maven build artifacts..."
mvn clean

echo "✅ Cleanup completed!"
