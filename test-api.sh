#!/bin/bash

echo "========================================"
echo "Testing Mock Authentication API"
echo "========================================"
echo ""

API_URL="http://localhost:8080"

# Test 1: Check if server is running
echo "1. Testing server health..."
curl -s "$API_URL/" | head -n 1
echo ""

# Test 2: Login with valid credentials
echo "2. Testing login with valid credentials (demo/password)..."
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password"}')

echo "$LOGIN_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$LOGIN_RESPONSE"
echo ""

# Extract tokens
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"refreshToken":"[^"]*' | cut -d'"' -f4)

# Test 3: Get user info with access token
echo "3. Testing authenticated endpoint (GET /auth/user)..."
curl -s -X GET "$API_URL/auth/user" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

# Test 4: Refresh token
echo "4. Testing token refresh..."
curl -s -X POST "$API_URL/auth/token/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" | python3 -m json.tool 2>/dev/null
echo ""

# Test 5: Invalid credentials
echo "5. Testing login with invalid credentials..."
curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"wrong","password":"wrong"}' | python3 -m json.tool 2>/dev/null
echo ""

# Test 6: Logout
echo "6. Testing logout..."
curl -s -X POST "$API_URL/auth/logout" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo "All tests completed!"
echo "========================================"
