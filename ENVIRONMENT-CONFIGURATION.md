# Multi-Environment Configuration Guide

## Overview

The SabPaisa Tokenization Backend supports multiple environments with dedicated configuration profiles:
- **local** - Local development with H2 in-memory database
- **dev** - Development environment on AWS with PostgreSQL
- **stage** - Staging environment for pre-production testing
- **prod** - Production environment with enhanced security and performance

## Environment Profiles

### 1. Local Development (`application-local.yml`)
- **Database**: H2 in-memory database
- **Purpose**: Rapid development without external dependencies
- **Features**:
  - H2 console enabled at `/h2-console`
  - Debug logging enabled
  - All actuator endpoints exposed
  - Mock SMS and email services
  - Encryption disabled for easier debugging
  - CORS configured for localhost origins

**To run locally:**
```bash
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### 2. Development Environment (`application-dev.yml`)
- **Database**: AWS RDS PostgreSQL
- **Purpose**: Integration testing with real services
- **Features**:
  - Real PostgreSQL database
  - Redis caching enabled
  - Swagger UI enabled
  - Debug logging for troubleshooting
  - Auto-update database schema
  - Flexible CORS configuration

**To run in dev:**
```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### 3. Staging Environment (`application-stage.yml`)
- **Database**: AWS RDS PostgreSQL (separate instance)
- **Purpose**: Pre-production testing and validation
- **Features**:
  - Production-like configuration
  - All features enabled for testing
  - Swagger UI enabled for API testing
  - More verbose logging than production
  - Test data generation capabilities

**To run in staging:**
```bash
./mvnw spring-boot:run -Dspring.profiles.active=stage
```

### 4. Production Environment (`application-prod.yml`)
- **Database**: AWS RDS PostgreSQL (high-availability)
- **Purpose**: Live production traffic
- **Features**:
  - Optimized for performance
  - Enhanced security settings
  - Minimal logging (WARN level)
  - Swagger UI disabled by default
  - All sensitive values from environment variables
  - Circuit breakers and rate limiting enabled

**To run in production:**
```bash
java -jar target/tokenization-backend.jar --spring.profiles.active=prod
```

## Environment Variables

### Common Variables (All Environments except Local)

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database JDBC URL | `jdbc:postgresql://host:5432/db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `secretpassword` |
| `REDIS_HOST` | Redis host | `redis.example.com` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password (if required) | `redispassword` |
| `JWT_SECRET_KEY` | JWT signing key | `your-secret-key` |
| `ENCRYPTION_MASTER_KEY` | Master key for encryption | `encryption-key` |

### Production-Specific Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET_KEY` | JWT signing key (no default) | Yes |
| `ENCRYPTION_MASTER_KEY` | Master encryption key | Yes |
| `MAIL_HOST` | SMTP server host | Yes |
| `MAIL_USERNAME` | SMTP username | Yes |
| `MAIL_PASSWORD` | SMTP password | Yes |
| `SMS_API_KEY` | SMS provider API key | Yes |
| `AWS_REGION` | AWS region | No (default: ap-south-1) |
| `S3_BUCKET` | S3 bucket name | Yes |
| `KMS_KEY_ID` | AWS KMS key ID | Yes |

### Optional Feature Flags

| Variable | Description | Default |
|----------|-------------|---------|
| `FEATURE_BIOMETRIC_ENABLED` | Enable biometric features | `true` |
| `FEATURE_ENTERPRISE_ENABLED` | Enable enterprise features | `true` |
| `FEATURE_BULK_TOKENIZATION_ENABLED` | Enable bulk operations | `true` |
| `FEATURE_FRAUD_DETECTION_ENABLED` | Enable fraud detection | `true` |
| `FEATURE_QUANTUM_ENCRYPTION_ENABLED` | Enable quantum encryption | `false` |
| `FEATURE_MULTI_CLOUD_ENABLED` | Enable multi-cloud features | `true` |

## Docker Configuration

### Building for Different Environments

```bash
# Local development
docker build --build-arg SPRING_PROFILES_ACTIVE=local -t sabpaisa/tokenization:local .

# Development
docker build --build-arg SPRING_PROFILES_ACTIVE=dev -t sabpaisa/tokenization:dev .

# Staging
docker build --build-arg SPRING_PROFILES_ACTIVE=stage -t sabpaisa/tokenization:stage .

# Production
docker build --build-arg SPRING_PROFILES_ACTIVE=prod -t sabpaisa/tokenization:prod .
```

### Running with Docker

```bash
# Local (H2 database)
docker run -p 8082:8082 sabpaisa/tokenization:local

# Development (requires environment variables)
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/sabpaisa_tokenization \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e REDIS_HOST=host.docker.internal \
  sabpaisa/tokenization:dev

# Production (all required variables)
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=$DB_URL \
  -e SPRING_DATASOURCE_USERNAME=$DB_USER \
  -e SPRING_DATASOURCE_PASSWORD=$DB_PASS \
  -e JWT_SECRET_KEY=$JWT_KEY \
  -e ENCRYPTION_MASTER_KEY=$ENCRYPT_KEY \
  # ... other required variables
  sabpaisa/tokenization:prod
```

## AWS Deployment

The GitHub Actions pipelines automatically handle environment-specific deployments:

1. **Infrastructure Setup**: Creates environment-specific resources
2. **Backend Deployment**: Deploys with appropriate Spring profile
3. **Environment Variables**: Pulled from AWS Secrets Manager and Parameter Store

### ECS Task Definition Environment Variables

The deployment pipeline automatically sets:
- `SPRING_PROFILES_ACTIVE`: Set to the deployment environment (dev/stage/prod)
- `SPRING_DATASOURCE_URL`: From AWS Parameter Store
- `SPRING_DATASOURCE_PASSWORD`: From AWS Secrets Manager
- Other environment-specific variables

## Best Practices

1. **Never commit sensitive values** - Use environment variables or AWS Secrets Manager
2. **Test configuration changes** - Always test in lower environments first
3. **Use appropriate logging levels** - Debug for dev, Info for stage, Warn for prod
4. **Monitor resource usage** - Adjust connection pools and memory settings per environment
5. **Feature flags** - Use them to control feature rollout across environments

## Troubleshooting

### Profile Not Loading
```bash
# Check active profiles
curl http://localhost:8082/actuator/env | jq '.activeProfiles'
```

### Database Connection Issues
```bash
# Test database connectivity
curl http://localhost:8082/actuator/health/db
```

### Configuration Values
```bash
# View current configuration (dev/stage only)
curl http://localhost:8082/actuator/configprops
```

## Security Notes

1. **Local Profile**: Only for local development, never deploy to cloud
2. **Dev Profile**: Limited access, can have debug features
3. **Stage Profile**: Should mirror production closely
4. **Prod Profile**: 
   - All sensitive values must be externalized
   - Swagger UI disabled by default
   - Minimal logging to prevent information leakage
   - Rate limiting and security features enabled