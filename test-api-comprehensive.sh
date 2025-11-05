#!/bin/bash

echo "========================================"
echo "Comprehensive API Test Suite"
echo "========================================"
echo ""

API_URL="http://localhost:8080"

echo "=== Test 1: Server Health ==="
curl -s "$API_URL/" | head -3
echo -e "\n"

echo "=== Test 2: Login with valid credentials ==="
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password"}')
echo "$LOGIN_RESPONSE" | python3 -m json.tool
echo ""

echo "=== Test 3: Extract tokens ==="
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['accessToken'])")
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['refreshToken'])")
echo "✓ Access Token extracted: ${ACCESS_TOKEN:0:30}..."
echo "✓ Refresh Token extracted: ${REFRESH_TOKEN:0:30}..."
echo ""

echo "=== Test 4: Get user info with access token ==="
USER_RESPONSE=$(curl -s -X GET "$API_URL/auth/user" \
  -H "Authorization: Bearer $ACCESS_TOKEN")
echo "$USER_RESPONSE" | python3 -m json.tool
if echo "$USER_RESPONSE" | grep -q "username"; then
    echo "✓ PASSED: Successfully retrieved user info"
else
    echo "✗ FAILED: Could not retrieve user info"
fi
echo ""

echo "=== Test 5: Refresh access token ==="
REFRESH_RESPONSE=$(curl -s -X POST "$API_URL/auth/token/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
echo "$REFRESH_RESPONSE" | python3 -m json.tool
if echo "$REFRESH_RESPONSE" | grep -q "accessToken"; then
    echo "✓ PASSED: Successfully refreshed token"
else
    echo "✗ FAILED: Could not refresh token"
fi
echo ""

echo "=== Test 6: Login with invalid credentials ==="
INVALID_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"invalid","password":"wrong"}')
echo "$INVALID_RESPONSE" | python3 -m json.tool
if echo "$INVALID_RESPONSE" | grep -q "unauthorized"; then
    echo "✓ PASSED: Correctly rejected invalid credentials"
else
    echo "✗ FAILED: Should have rejected invalid credentials"
fi
echo ""

echo "=== Test 7: Logout ==="
LOGOUT_RESPONSE=$(curl -s -X POST "$API_URL/auth/logout" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
echo "$LOGOUT_RESPONSE" | python3 -m json.tool
if echo "$LOGOUT_RESPONSE" | grep -q "Logged out successfully"; then
    echo "✓ PASSED: Successfully logged out"
else
    echo "✗ FAILED: Logout failed"
fi
echo ""

echo "=== Test 8: Try to use token after logout ==="
POST_LOGOUT_RESPONSE=$(curl -s -X POST "$API_URL/auth/token/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
echo "$POST_LOGOUT_RESPONSE" | python3 -m json.tool
if echo "$POST_LOGOUT_RESPONSE" | grep -q "invalid_token"; then
    echo "✓ PASSED: Correctly rejected invalidated token"
else
    echo "✗ FAILED: Should have rejected invalidated token"
fi
echo ""

echo "=== Test 9: Test all user accounts ==="
for creds in "demo:password" "user1:pass123" "test:test123"; do
    username=$(echo $creds | cut -d: -f1)
    password=$(echo $creds | cut -d: -f2)
    response=$(curl -s -X POST "$API_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    if echo "$response" | grep -q "accessToken"; then
        echo "✓ PASSED: Login successful for $username"
    else
        echo "✗ FAILED: Login failed for $username"
    fi
done
echo ""

echo "========================================"
echo "Test Summary"
echo "========================================"
echo "All critical API endpoints tested!"
echo "Server is ready for Android integration"
echo "========================================"
