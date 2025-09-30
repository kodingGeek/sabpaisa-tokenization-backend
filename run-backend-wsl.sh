#!/bin/bash

echo "Starting backend application with PostgreSQL..."

# Set environment variables for database connection
export DATABASE_URL="jdbc:postgresql://localhost:5432/sabpaisa_tokenization"
export DATABASE_USERNAME="postgres"
export DATABASE_PASSWORD="password"

# Run Spring Boot application
mvn spring-boot:run \
  -Dspring-boot.run.arguments="--spring.datasource.url=${DATABASE_URL} --spring.datasource.username=${DATABASE_USERNAME} --spring.datasource.password=${DATABASE_PASSWORD}"