# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Add wait-for-it script to wait for database
RUN apk add --no-cache bash curl postgresql-client

# Create a simple wait script
RUN echo '#!/bin/bash' > /wait-for-postgres.sh && \
    echo 'until pg_isready -h postgres -p 5432 -U postgres; do' >> /wait-for-postgres.sh && \
    echo '  echo "Waiting for PostgreSQL..."' >> /wait-for-postgres.sh && \
    echo '  sleep 2' >> /wait-for-postgres.sh && \
    echo 'done' >> /wait-for-postgres.sh && \
    echo 'echo "PostgreSQL is ready!"' >> /wait-for-postgres.sh && \
    chmod +x /wait-for-postgres.sh

EXPOSE 8082

# Set environment variables for Spring Boot
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-docker}
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# Run the application with environment variable substitution
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]