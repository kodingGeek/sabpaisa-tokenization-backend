#!/bin/bash

# Script to run the application with different environment profiles

echo "SabPaisa Tokenization Service - Environment Runner"
echo "================================================="

# Check if environment is provided
if [ $# -eq 0 ]; then
    echo "Usage: ./run-env.sh [local|dev|stage|prod]"
    echo ""
    echo "Available environments:"
    echo "  local - Local development with H2 database"
    echo "  dev   - Development environment"
    echo "  stage - Staging environment"
    echo "  prod  - Production environment"
    exit 1
fi

ENV=$1
PROFILE=""

case $ENV in
    local)
        PROFILE="local"
        echo "Starting LOCAL environment with H2 database..."
        echo "H2 Console will be available at: http://localhost:8082/h2-console"
        echo "Swagger UI: http://localhost:8082/swagger-ui.html"
        ;;
    dev)
        PROFILE="dev"
        echo "Starting DEVELOPMENT environment..."
        echo "Make sure environment variables are set or use .env file"
        echo "Swagger UI: http://localhost:8082/api/swagger-ui.html"
        ;;
    stage)
        PROFILE="stage"
        echo "Starting STAGING environment..."
        echo "Make sure environment variables are set"
        echo "Swagger UI: http://localhost:8082/api/swagger-ui.html"
        ;;
    prod)
        PROFILE="prod"
        echo "Starting PRODUCTION environment..."
        echo "WARNING: Swagger UI is disabled by default"
        echo "Make sure all required environment variables are set!"
        
        # Check critical environment variables for production
        if [ -z "$JWT_SECRET_KEY" ] || [ -z "$ENCRYPTION_MASTER_KEY" ]; then
            echo "ERROR: Critical environment variables are missing!"
            echo "Required: JWT_SECRET_KEY, ENCRYPTION_MASTER_KEY"
            exit 1
        fi
        ;;
    *)
        echo "Invalid environment: $ENV"
        echo "Use: local, dev, stage, or prod"
        exit 1
        ;;
esac

echo ""
echo "Starting application with profile: $PROFILE"
echo "Press Ctrl+C to stop"
echo ""

# Run the application with the selected profile
# Use mvnw if available, otherwise use mvn
if [ -f "./mvnw" ]; then
    ./mvnw spring-boot:run -Dspring.profiles.active=$PROFILE
else
    mvn spring-boot:run -Dspring.profiles.active=$PROFILE
fi