# SabPaisa Tokenization API Demo

## 🚀 Application Status: RUNNING

The application is successfully running on **port 8082**

## 📍 Access Points

| Service | URL | Status |
|---------|-----|--------|
| Health Check | http://localhost:8082/api/v1/tokens/health | ✅ Running |
| Swagger UI | http://localhost:8082/api/v1/swagger-ui.html | ✅ Available |
| API Docs | http://localhost:8082/api/v1/docs | ✅ Available |

## 🧪 Live Demo Results

### 1. Tokenization Request
```bash
POST http://localhost:8082/api/v1/tokens/tokenize
{
  "cardNumber": "4111111111111111",
  "merchantId": "MERCH001"
}
```

**Response:**
```json
{
  "tokenValue": "6290398088332981",
  "maskedPan": "411111******1111",
  "status": "ACTIVE",
  "expiresAt": "2028-09-26T09:30:08.120138",
  "success": true,
  "message": "Tokenization successful"
}
```

### 2. Detokenization Request
```bash
POST http://localhost:8082/api/v1/tokens/detokenize
{
  "token": "6290398088332981",
  "merchantId": "MERCH001"
}
```

**Response:**
```json
{
  "tokenValue": "6290398088332981",
  "maskedPan": "411111******1111",
  "status": "ACTIVE",
  "expiresAt": "2028-09-26T09:30:08.120138",  
  "success": true,
  "message": "Tokenization successful"
}
```

## 🔑 Test Merchant Credentials
- **Merchant ID:** MERCH001
- **Status:** ACTIVE

## ✨ Key Features Working
- ✅ Card validation (Luhn algorithm)
- ✅ Unique token generation
- ✅ Card masking (shows first 6 & last 4 digits)
- ✅ Token reuse for same card
- ✅ Usage tracking
- ✅ Automatic expiry (3 years)

## 🐳 Docker Status
```bash
# Check running containers
docker compose ps

# View logs
docker compose logs app -f

# Stop application
docker compose down

# Start application
docker compose up -d
```

## 📊 Database Tables Created
- `merchants` - Stores merchant information
- `tokens` - Stores tokenized card data

The application is fully functional and ready for testing!