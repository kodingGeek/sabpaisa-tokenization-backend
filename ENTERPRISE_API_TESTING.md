# Enterprise Features API Testing Guide

## Base URL
```
http://localhost:8082/api/v1
```

## Required Headers
- `X-Merchant-Id`: Your merchant ID (e.g., "MERCHANT001")
- `Content-Type`: application/json
- `X-Encryption-Enabled`: true (optional, for encrypted requests/responses)

## 1. Platform Management

### Create a Platform
```bash
curl -X POST http://localhost:8082/api/v1/platform-tokens/platforms \
  -H "Content-Type: application/json" \
  -H "X-Merchant-Id: MERCHANT001" \
  -d '{
    "platformCode": "ECOM_MAIN",
    "platformName": "Main E-Commerce Site",
    "description": "Primary e-commerce platform",
    "iconUrl": "https://example.com/icon.png",
    "webhookUrl": "https://example.com/webhook",
    "allowedDomains": "example.com,www.example.com"
  }'
```

### Get Available Platforms
```bash
curl http://localhost:8082/api/v1/platform-tokens/platforms \
  -H "X-Merchant-Id: MERCHANT001"
```

## 2. Token Type Management

### Get Available Token Types
```bash
curl http://localhost:8082/api/v1/platform-tokens/token-types
```

Response will include:
- COF (Card on File) - 365 days
- FPT (Fast Payment Token) - 180 days  
- OTT (One-Time Token) - 1 day
- GUEST (Guest Checkout) - 30 days
- SUBSCRIPTION - 730 days

## 3. Platform-Based Tokenization

### Create a Platform Token
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
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+919876543210",
    "customerId": "CUST001",
    "enableNotifications": true,
    "daysBeforeExpiryNotification": 30
  }'
```

### Get Tokens for a Card (Multi-Platform View)
```bash
# First, get the card hash from a tokenization response
# Then use it to query all tokens
curl http://localhost:8082/api/v1/platform-tokens/card/{cardHash} \
  -H "X-Merchant-Id: MERCHANT001"
```

## 4. Bulk Retokenization

### Retokenize All Expired Tokens
```bash
curl -X POST http://localhost:8082/api/v1/tokens/bulk/retokenize \
  -H "Content-Type: application/json" \
  -H "X-Merchant-Id: MERCHANT001" \
  -d '{
    "selectionCriteria": "EXPIRED",
    "newExpiryMonths": 12,
    "sendNotification": true
  }'
```

### Retokenize Tokens Expiring Soon
```bash
curl -X POST http://localhost:8082/api/v1/tokens/bulk/retokenize \
  -H "Content-Type: application/json" \
  -H "X-Merchant-Id: MERCHANT001" \
  -d '{
    "selectionCriteria": "EXPIRING_SOON",
    "daysBeforeExpiry": 30,
    "newExpiryMonths": 12,
    "sendNotification": true
  }'
```

### Retokenize by Platform
```bash
curl -X POST http://localhost:8082/api/v1/tokens/bulk/retokenize \
  -H "Content-Type: application/json" \
  -H "X-Merchant-Id: MERCHANT001" \
  -d '{
    "selectionCriteria": "SPECIFIC_PLATFORM",
    "platformId": 1,
    "newExpiryMonths": 12,
    "sendNotification": false
  }'
```

### Check Bulk Operation Status
```bash
curl http://localhost:8082/api/v1/tokens/bulk/status/{requestId} \
  -H "X-Merchant-Id: MERCHANT001"
```

## 5. Token Monetization / Billing

### Get Billing Dashboard
```bash
curl http://localhost:8082/api/v1/billing/dashboard \
  -H "X-Merchant-Id: MERCHANT001"
```

Response includes:
- Current month usage statistics
- Estimated charges breakdown
- Historical billing records
- Usage trends and growth rates
- Platform-wise token distribution

### Generate Invoice
```bash
curl -X POST http://localhost:8082/api/v1/billing/generate-invoice/{billingRecordId} \
  -H "X-Merchant-Id: MERCHANT001"
```

## 6. API Encryption Testing

### Send Encrypted Request
```bash
# First, encrypt your payload using AES-256-GCM
# Then send it with encryption header

curl -X POST http://localhost:8082/api/v1/platform-tokens/tokenize \
  -H "Content-Type: application/json" \
  -H "X-Merchant-Id: MERCHANT001" \
  -H "X-Encryption-Enabled: true" \
  -d '{
    "encryptedData": "BASE64_ENCRYPTED_PAYLOAD",
    "algorithm": "AES-256-GCM",
    "timestamp": 1234567890
  }'
```

## 7. Testing Scenarios

### Scenario 1: Multi-Platform Token Creation
1. Create 3 different platforms (ECOMMERCE, MOBILE_APP, SUBSCRIPTION)
2. Create tokens for the same card on each platform
3. Query all tokens for that card
4. Verify each platform has its own token

### Scenario 2: Token Expiry Management
1. Create tokens with different expiry settings
2. Wait for notification scheduling (runs daily at 9 AM)
3. Check notification logs
4. Use bulk retokenization to renew expiring tokens

### Scenario 3: Billing Cycle
1. Create multiple tokens throughout the day
2. Generate transactions using the tokens
3. Check billing dashboard for real-time usage
4. Verify charge calculations

### Scenario 4: Platform Token Limits
1. Create a token type with maxTokensPerCard = 3
2. Try creating 4 tokens for the same card
3. Verify the 4th token creation fails

## 8. Error Testing

### Invalid Platform
```bash
curl -X POST http://localhost:8082/api/v1/platform-tokens/tokenize \
  -H "Content-Type: application/json" \
  -H "X-Merchant-Id: MERCHANT001" \
  -d '{
    "cardNumber": "4111111111111111",
    "expiryDate": "12/25",
    "cvv": "123",
    "platformId": 9999,
    "tokenTypeCode": "COF"
  }'
```

### Invalid Token Type
```bash
curl -X POST http://localhost:8082/api/v1/platform-tokens/tokenize \
  -H "Content-Type: application/json" \
  -H "X-Merchant-Id: MERCHANT001" \
  -d '{
    "cardNumber": "4111111111111111",
    "expiryDate": "12/25",
    "cvv": "123",
    "platformId": 1,
    "tokenTypeCode": "INVALID_TYPE"
  }'
```

## 9. Performance Testing

### Bulk Token Creation
```bash
# Create 100 tokens in a loop
for i in {1..100}; do
  curl -X POST http://localhost:8082/api/v1/platform-tokens/tokenize \
    -H "Content-Type: application/json" \
    -H "X-Merchant-Id: MERCHANT001" \
    -d '{
      "cardNumber": "4111111111111'$i'",
      "expiryDate": "12/25",
      "cvv": "123",
      "platformId": 1,
      "tokenTypeCode": "COF"
    }' &
done
wait
```

## 10. Webhook Testing

Platforms can have webhook URLs for token events. Test by:
1. Creating a platform with webhook URL
2. Creating/updating tokens
3. Monitoring webhook calls

## Expected Responses

### Successful Token Creation
```json
{
  "tokenId": 1,
  "tokenValue": "1234567890123456",
  "platformCode": "ECOM_MAIN",
  "platformName": "Main E-Commerce Site",
  "tokenType": "COF",
  "expiryDate": "2024-12-25T00:00:00",
  "maskedPan": "411111******1111",
  "cardBrand": "VISA",
  "cardType": "CREDIT"
}
```

### Billing Dashboard
```json
{
  "currentMonthUsage": {
    "totalTokensCreated": 150,
    "totalActiveTokens": 1200,
    "totalTransactions": 5000,
    "platformBreakdown": {
      "ECOMMERCE": 80,
      "MOBILE_APP": 50,
      "SUBSCRIPTION": 20
    }
  },
  "estimatedCharges": {
    "tokenCreationCharges": 15.00,
    "storageCharges": 60.00,
    "transactionCharges": 100.00,
    "platformCharges": 1000.00,
    "subtotal": 1175.00,
    "taxAmount": 211.50,
    "totalAmount": 1386.50
  }
}
```