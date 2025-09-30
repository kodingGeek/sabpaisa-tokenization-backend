#!/bin/bash

echo "=== Running SabPaisa Tokenization Backend with Enterprise Features ==="
echo ""
echo "Prerequisites Check:"
echo "-------------------"

# Check Java
if command -v java &> /dev/null; then
    echo "✓ Java found: $(java -version 2>&1 | head -n 1)"
else
    echo "✗ Java not found. Please install Java 17"
    echo "  Set JAVA_HOME and add to PATH"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    echo "✓ Maven found: $(mvn -version | head -n 1)"
else
    echo "✗ Maven not found. Please install Maven 3.6+"
    exit 1
fi

echo ""
echo "Service Status:"
echo "--------------"

# Check PostgreSQL
if nc -z localhost 5432 2>/dev/null; then
    echo "✓ PostgreSQL is running on port 5432"
else
    echo "✗ PostgreSQL is not accessible on port 5432"
    echo "  Please start PostgreSQL with database 'sabpaisa_tokenization'"
fi

# Check Redis
if nc -z localhost 6379 2>/dev/null; then
    echo "✓ Redis is running on port 6379"
else
    echo "✗ Redis is not accessible on port 6379"
    echo "  Please start Redis server"
fi

echo ""
echo "Building and Running Application..."
echo "---------------------------------"

# Set environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sabpaisa_tokenization
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=password
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379

# Clean and compile
echo "Compiling..."
mvn clean compile

# Run the application
echo ""
echo "Starting application on port 8082..."
echo "===================================="
mvn spring-boot:run