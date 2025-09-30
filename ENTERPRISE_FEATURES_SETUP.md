# Enterprise Features Setup Guide

This guide will help you run the tokenization backend with the new enterprise features.

## Prerequisites

1. **PostgreSQL** (version 12 or higher)
2. **Redis** (version 6 or higher)
3. **Java 17**
4. **Maven 3.6+**

## Setup Instructions

### 1. Database Setup

Since Docker is not available, you'll need to set up PostgreSQL and Redis manually:

#### PostgreSQL Setup
```bash
# If PostgreSQL is installed locally, create the database:
createdb sabpaisa_tokenization

# Connect to PostgreSQL and run the migration scripts:
psql -U postgres -d sabpaisa_tokenization
```

Then run these SQL files in order:
1. `/database/scripts/01_create_database.sql`
2. `/database/scripts/02_create_core_tables.sql`
3. `/src/main/resources/db/migration/V2__enterprise_features.sql` (New enterprise tables)

#### Redis Setup
Start Redis on default port 6379.

### 2. Generate Encryption Master Key

The application requires a master key for AES-256-GCM encryption. Generate one using this Java code:

```java
import javax.crypto.KeyGenerator;
import java.security.SecureRandom;
import java.util.Base64;

public class GenerateKey {
    public static void main(String[] args) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        byte[] key = keyGen.generateKey().getEncoded();
        System.out.println("Master Key: " + Base64.getEncoder().encodeToString(key));
    }
}
```

Or use this pre-generated key for testing:
```
app.encryption.master-key=1J+YW8KbKvFJGj1P3nPMoxyKgdJV4PwMZRb5H5wpLZ8=
```

### 3. Update Application Configuration

Edit `src/main/resources/application.yml` and add the master key:

```yaml
app:
  encryption:
    master-key: YOUR_GENERATED_KEY_HERE
```

### 4. Environment Variables

Set these environment variables or update application.yml:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sabpaisa_tokenization
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=password
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379
```

### 5. Run the Application

```bash
# Navigate to backend directory
cd /mnt/d/Manish/AI-hackathon-Tokenization/sabpaisa-tokenization/backend

# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

Or with all environment variables:
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sabpaisa_tokenization \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=password \
SPRING_REDIS_HOST=localhost \
SPRING_REDIS_PORT=6379 \
mvn spring-boot:run
```

## New Enterprise Features

### 1. Platform-Based Tokenization
- **Endpoint**: `POST /api/v1/platform-tokens/tokenize`
- Create tokens for specific platforms
- One card can have multiple tokens across different platforms

### 2. Token Types
- **COF** (Card on File) - 365 days default expiry
- **FPT** (Fast Payment Token) - 180 days default expiry
- **OTT** (One-Time Token) - 1 day expiry
- **GUEST** - 30 days expiry
- **SUBSCRIPTION** - 730 days expiry

### 3. Bulk Retokenization
- **Endpoint**: `POST /api/v1/tokens/bulk/retokenize`
- Renew multiple tokens at once
- Selection criteria: expired, expiring soon, by platform, specific tokens, date range

### 4. Token Monetization
- **Endpoint**: `GET /api/v1/billing/dashboard`
- View billing dashboard with usage metrics
- Automatic monthly billing calculation
- Free tier: 1000 tokens/month

### 5. API Encryption
- All API requests/responses can be encrypted using AES-256-GCM
- Add header `X-Encryption-Enabled: true` to enable encryption

### 6. Platform Management
- **Endpoint**: `POST /api/v1/platform-tokens/platforms`
- Create and manage merchant platforms

## Testing the Features

### 1. Create a Platform
```bash
curl -X POST http://localhost:8082/api/v1/platform-tokens/platforms \
  -H "Content-Type: application/json" \
  -H "X-Merchant-Id: MERCHANT001" \
  -d '{
    "platformCode": "ECOMMERCE",
    "platformName": "E-Commerce Platform",
    "description": "Main e-commerce website"
  }'
```

### 2. Create a Platform Token
```bash
curl -X POST http://localhost:8082/api/v1/platform-tokens/tokenize \
  -H "Content-Type: application/json" \
  -H "X-Merchant-Id: MERCHANT001" \
  -d '{
    "cardNumber": "4111111111111111",
    "expiryDate": "12/25",
    "cvv": "123",
    "platformId": 1,
    "tokenTypeCode": "COF",
    "customerEmail": "customer@example.com",
    "enableNotifications": true
  }'
```

### 3. View Billing Dashboard
```bash
curl http://localhost:8082/api/v1/billing/dashboard \
  -H "X-Merchant-Id: MERCHANT001"
```

## Database Schema

The new enterprise features add these tables:
- `platforms` - Platform definitions
- `token_types` - Token type configurations
- `enhanced_tokens` - Enhanced token storage with platform support
- `token_usage` - Token usage tracking
- `pricing_plans` - Monetization pricing plans
- `billing_records` - Monthly billing records

## Troubleshooting

### PostgreSQL Connection Issues
- Ensure PostgreSQL is running on port 5432
- Check username/password in application.yml
- Verify database 'sabpaisa_tokenization' exists

### Redis Connection Issues
- Ensure Redis is running on port 6379
- Check if Redis requires authentication

### Java/Maven Issues
- Ensure JAVA_HOME is set to Java 17 installation
- Add Java to PATH: `export PATH=$JAVA_HOME/bin:$PATH`
- Verify with: `java -version` and `mvn -version`

### Missing Dependencies
Run: `mvn dependency:resolve` to download all dependencies

## Security Notes

1. **Never commit the master encryption key** to version control
2. Use different keys for different environments
3. Rotate keys periodically
4. Enable HTTPS in production
5. Configure proper CORS settings

## Support

For issues or questions:
- Check logs in console output
- Review error messages in API responses
- Ensure all prerequisites are installed and running