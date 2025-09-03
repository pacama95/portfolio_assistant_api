#!/bin/bash

# Quick Portfolio API Test Script
# Simple script for basic API functionality testing

set -e

BASE_URL="${API_BASE_URL:-http://localhost:8080}"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}Portfolio API Quick Test${NC}"
echo "========================="
echo "Testing API at: $BASE_URL"
echo

# Test API health
echo "🔍 Testing API health..."
if curl -s --connect-timeout 5 "$BASE_URL/api/transactions/count" >/dev/null 2>&1; then
    echo -e "${GREEN}✅ API is responding${NC}"
else
    echo -e "${RED}❌ API is not accessible${NC}"
    exit 1
fi

# Create a test transaction
echo "📝 Creating test transaction..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "TEST",
        "transactionType": "BUY",
        "quantity": 1.0,
        "price": 100.00,
        "currency": "USD",
        "transactionDate": "2024-01-01",
        "notes": "Quick test transaction"
    }')

if echo "$RESPONSE" | grep -q '"id"'; then
    echo -e "${GREEN}✅ Transaction created successfully${NC}"
    TRANSACTION_ID=$(echo "$RESPONSE" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
    echo "   Transaction ID: $TRANSACTION_ID"
else
    echo -e "${RED}❌ Failed to create transaction${NC}"
    echo "Response: $RESPONSE"
    exit 1
fi

# Get positions
echo "📊 Checking positions..."
POSITIONS=$(curl -s "$BASE_URL/api/positions/active")
if [ -n "$POSITIONS" ]; then
    echo -e "${GREEN}✅ Positions retrieved successfully${NC}"
else
    echo -e "${RED}❌ Failed to get positions${NC}"
fi

# Update market price
echo "💰 Updating market price..."
PRICE_UPDATE=$(curl -s -X PUT "$BASE_URL/api/positions/ticker/TEST/price" \
    -H "Content-Type: application/json" \
    -d '{"price": 105.00}')

if echo "$PRICE_UPDATE" | grep -q '"currentPrice"'; then
    echo -e "${GREEN}✅ Market price updated successfully${NC}"
else
    echo -e "${RED}❌ Failed to update market price${NC}"
fi

# Get portfolio summary
echo "📈 Getting portfolio summary..."
SUMMARY=$(curl -s "$BASE_URL/api/portfolio/summary/active")
if echo "$SUMMARY" | grep -q '"totalMarketValue"'; then
    echo -e "${GREEN}✅ Portfolio summary retrieved successfully${NC}"
    
    # Extract and display key metrics
    MARKET_VALUE=$(echo "$SUMMARY" | sed -n 's/.*"totalMarketValue":\([^,}]*\).*/\1/p')
    TOTAL_COST=$(echo "$SUMMARY" | sed -n 's/.*"totalCost":\([^,}]*\).*/\1/p')
    POSITIONS_COUNT=$(echo "$SUMMARY" | sed -n 's/.*"activePositions":\([^,}]*\).*/\1/p')
    
    echo "   Market Value: $MARKET_VALUE"
    echo "   Total Cost: $TOTAL_COST"
    echo "   Active Positions: $POSITIONS_COUNT"
else
    echo -e "${RED}❌ Failed to get portfolio summary${NC}"
fi

# Cleanup
echo "🧹 Cleaning up test data..."
if [ -n "$TRANSACTION_ID" ]; then
    DELETE_RESPONSE=$(curl -s -w "%{http_code}" -X DELETE "$BASE_URL/api/transactions/$TRANSACTION_ID")
    if [[ "$DELETE_RESPONSE" == *"204"* ]]; then
        echo -e "${GREEN}✅ Test transaction deleted successfully${NC}"
    else
        echo -e "${RED}❌ Failed to delete test transaction${NC}"
    fi
fi

echo
echo -e "${GREEN}🎉 Quick test completed successfully!${NC}"
echo "To run comprehensive tests, use: ./scripts/test-api.sh" 