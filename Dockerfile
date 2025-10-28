# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven files and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM amazoncorretto:17-alpine
WORKDIR /app

# Install required packages for health checks and add non-root user
RUN apk add --no-cache curl wget && \
    adduser -D -s /bin/sh appuser

# Copy war/jar from build stage
COPY --from=build /app/target/*.war /app/target/*.jar ./

# Rename to app.jar for consistency
RUN mv *.war app.jar 2>/dev/null || mv *.jar app.jar

# Set ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/tokenization-backend/actuator/health || exit 1

# Set JVM options optimized for containers
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Use production profile for ECS deployment
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT exec java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app.jar