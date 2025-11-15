#!/bin/bash
set -e  # Exit on error

echo "Building all Docker images..."
docker compose build

echo "Starting all containers..."
docker compose up -d

echo "Verifying containers..."
docker ps --filter "name=sentiment"

echo "All ktor services are running!"
echo "Frontend: http://localhost:8881"
echo "Analyzer:  http://localhost:8883"
echo "Collector: http://localhost:8882"
