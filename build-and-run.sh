#!/bin/bash

echo "=== Building and Running SabPaisa Tokenization Backend ==="

# Start infrastructure
echo "Starting infrastructure services..."
docker compose up -d postgres redis

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 10

# Build with Docker
echo "Building application with Maven..."
docker run --rm \
  -v "$(pwd)":/app \
  -v "$HOME/.m2":/root/.m2 \
  -w /app \
  --network host \
  maven:3.9.0-eclipse-temurin-17 \
  mvn clean compile

# Run application
echo "Running application..."
docker run --rm \
  -v "$(pwd)":/app \
  -v "$HOME/.m2":/root/.m2 \
  -w /app \
  --network host \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sabpaisa_tokenization \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e SPRING_REDIS_HOST=localhost \
  -e SPRING_REDIS_PORT=6379 \
  maven:3.9.0-eclipse-temurin-17 \
  mvn spring-boot:run