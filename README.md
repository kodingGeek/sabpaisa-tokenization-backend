# SabPaisa Tokenization Backend - Phase 1

## Overview
This is a simplified tokenization backend built without Lombok, focusing on core functionality first.

## Features Implemented (Phase 1)
- ✅ Basic tokenization and detokenization
- ✅ Merchant management
- ✅ PostgreSQL database integration
- ✅ Redis caching support
- ✅ RESTful API endpoints
- ✅ Basic security configuration
- ✅ Test merchant auto-creation

## Running the Application

The application is currently running on port 8080. A test merchant has been created:
- Merchant ID: MERCH001
- API Key: 66b6d29e-e885-460c-ab2c-0871ff5ca3cf
- API Secret: 8c796343-6ed4-4921-9a98-2d0549065a8b

## API Endpoints

### 1. Tokenize Card
```bash
curl -X POST http://localhost:8080/api/v1/tokens/tokenize \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111111",
    "merchantId": "MERCH001"
  }'
```

### 2. Detokenize
```bash
curl -X POST http://localhost:8080/api/v1/tokens/detokenize \
  -H "Content-Type: application/json" \
  -d '{
    "token": "1234567890123456",
    "merchantId": "MERCH001"
  }'
```

### 3. Health Check
```bash
curl http://localhost:8080/api/v1/tokens/health
```

## Swagger UI
Access the API documentation at: http://localhost:8080/api/v1/swagger-ui.html

## Next Phases
- Phase 2: JWT Authentication
- Phase 3: Advanced tokenization algorithms
- Phase 4: Bulk operations
- Phase 5: Audit and compliance
- Phase 6: Performance optimization

## Technology Stack
- Java 17
- Spring Boot 3.1.5
- PostgreSQL 15
- Redis 7
- No Lombok (plain Java)

## Project Structure
```
backend/
├── src/main/java/com/sabpaisa/tokenization/
│   ├── controller/      # REST endpoints
│   ├── service/        # Business logic
│   ├── repository/     # Data access
│   ├── entity/         # JPA entities
│   ├── dto/           # Request/Response DTOs
│   ├── config/        # Configuration
│   └── security/      # Security config
├── docker-compose.yml  # Infrastructure
└── build-and-run.sh   # Build script
```