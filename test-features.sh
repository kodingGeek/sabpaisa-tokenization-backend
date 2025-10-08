#!/bin/bash

# Test script for advanced tokenization features

API_BASE="http://localhost:8082/api/v1"

echo "=== Testing Tokenization Features ==="
echo ""

# Test 1: Basic Merchant and Token APIs (should work on all branches)
echo "1. Testing basic merchant API..."
curl -s "$API_BASE/merchants" | jq '.merchants[0].merchantId' 2>/dev/null || echo "Merchant API test failed"
echo ""

# Test 2: Fraud Detection API
echo "2. Testing fraud detection API..."
curl -s -X POST "$API_BASE/fraud/check" \
  -H "Content-Type: application/json" \
  -d '{
    "tokenId": "TOK-123",
    "transactionAmount": 5000,
    "merchantId": "MERCH001",
    "ipAddress": "192.168.1.100",
    "deviceFingerprint": "test-device-001"
  }' | jq '.riskScore' 2>/dev/null || echo "Fraud detection API not available on this branch"
echo ""

# Test 3: Quantum Security Status
echo "3. Testing quantum security API..."
curl -s "$API_BASE/quantum-security/status" | jq '.quantumEnabled' 2>/dev/null || echo "Quantum security API not available on this branch"
echo ""

# Test 4: Biometric System Status
echo "4. Testing biometric API..."
curl -s "$API_BASE/biometric/system/status" | jq '.capabilities' 2>/dev/null || echo "Biometric API not available on this branch"
echo ""

# Test 5: Multi-Cloud Status
echo "5. Testing multi-cloud API..."
curl -s "$API_BASE/multi-cloud/status" | jq '.healthStatus' 2>/dev/null || echo "Multi-cloud API not available on this branch"
echo ""

echo "=== Feature Test Complete ==="