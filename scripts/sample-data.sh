#!/bin/bash

# Sample Data Population Script
# Populates the portfolio system with realistic sample data

set -e

BASE_URL="${API_BASE_URL:-http://localhost:8080}"
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}Portfolio Sample Data Population${NC}"
echo "=================================="
echo "Target API: $BASE_URL"
echo

# Check API health
echo "🔍 Checking API health..."
if ! curl -s --connect-timeout 5 "$BASE_URL/api/transactions/count" >/dev/null 2>&1; then
    echo "❌ API is not accessible at $BASE_URL"
    exit 1
fi
echo -e "${GREEN}✅ API is accessible${NC}"

# Create sample transactions
echo
echo "📝 Creating sample transactions..."

# Apple transactions
echo "Creating Apple (AAPL) transactions..."
curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "AAPL",
        "transactionType": "BUY",
        "quantity": 50.0,
        "price": 145.00,
        "fees": 9.99,
        "currency": "USD",
        "transactionDate": "2024-01-15",
        "notes": "Initial Apple investment"
    }' >/dev/null

curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "AAPL",
        "transactionType": "BUY",
        "quantity": 25.0,
        "price": 152.50,
        "fees": 7.99,
        "currency": "USD",
        "transactionDate": "2024-02-20",
        "notes": "Additional Apple shares"
    }' >/dev/null

curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "AAPL",
        "transactionType": "DIVIDEND",
        "quantity": 75.0,
        "price": 0.24,
        "fees": 0.00,
        "currency": "USD",
        "transactionDate": "2024-03-15",
        "notes": "Q1 2024 dividend"
    }' >/dev/null

# Microsoft transactions
echo "Creating Microsoft (MSFT) transactions..."
curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "MSFT",
        "transactionType": "BUY",
        "quantity": 30.0,
        "price": 380.00,
        "fees": 12.99,
        "currency": "USD",
        "transactionDate": "2024-01-25",
        "notes": "Microsoft investment"
    }' >/dev/null

curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "MSFT",
        "transactionType": "BUY",
        "quantity": 15.0,
        "price": 395.75,
        "fees": 8.99,
        "currency": "USD",
        "transactionDate": "2024-03-10",
        "notes": "Additional Microsoft shares"
    }' >/dev/null

# Google transactions
echo "Creating Google (GOOGL) transactions..."
curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "GOOGL",
        "transactionType": "BUY",
        "quantity": 20.0,
        "price": 2750.00,
        "fees": 15.99,
        "currency": "USD",
        "transactionDate": "2024-02-05",
        "notes": "Google Alphabet investment"
    }' >/dev/null

# Tesla transactions
echo "Creating Tesla (TSLA) transactions..."
curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "TSLA",
        "transactionType": "BUY",
        "quantity": 40.0,
        "price": 185.00,
        "fees": 11.99,
        "currency": "USD",
        "transactionDate": "2024-01-30",
        "notes": "Tesla investment"
    }' >/dev/null

curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "TSLA",
        "transactionType": "SELL",
        "quantity": 15.0,
        "price": 195.50,
        "fees": 8.99,
        "currency": "USD",
        "transactionDate": "2024-03-20",
        "notes": "Partial Tesla sale"
    }' >/dev/null

# Amazon transactions
echo "Creating Amazon (AMZN) transactions..."
curl -s -X POST "$BASE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -d '{
        "ticker": "AMZN",
        "transactionType": "BUY",
        "quantity": 35.0,
        "price": 3200.00,
        "fees": 18.99,
        "currency": "USD",
        "transactionDate": "2024-02-15",
        "notes": "Amazon investment"
    }' >/dev/null

echo -e "${GREEN}✅ Sample transactions created${NC}"

# Wait for positions to be calculated
sleep 2

# Update market prices to realistic current values
echo
echo "💰 Updating market prices..."

echo "Updating AAPL price..."
curl -s -X PUT "$BASE_URL/api/positions/ticker/AAPL/price" \
    -H "Content-Type: application/json" \
    -d '{"price": 175.50}' >/dev/null

echo "Updating MSFT price..."
curl -s -X PUT "$BASE_URL/api/positions/ticker/MSFT/price" \
    -H "Content-Type: application/json" \
    -d '{"price": 420.75}' >/dev/null

echo "Updating GOOGL price..."
curl -s -X PUT "$BASE_URL/api/positions/ticker/GOOGL/price" \
    -H "Content-Type: application/json" \
    -d '{"price": 2850.25}' >/dev/null

echo "Updating TSLA price..."
curl -s -X PUT "$BASE_URL/api/positions/ticker/TSLA/price" \
    -H "Content-Type: application/json" \
    -d '{"price": 205.00}' >/dev/null

echo "Updating AMZN price..."
curl -s -X PUT "$BASE_URL/api/positions/ticker/AMZN/price" \
    -H "Content-Type: application/json" \
    -d '{"price": 3350.80}' >/dev/null

echo -e "${GREEN}✅ Market prices updated${NC}"

# Display summary
echo
echo "📊 Portfolio Summary:"
SUMMARY=$(curl -s "$BASE_URL/api/portfolio/summary/active")
if echo "$SUMMARY" | grep -q '"totalMarketValue"'; then
    MARKET_VALUE=$(echo "$SUMMARY" | sed -n 's/.*"totalMarketValue":\([^,}]*\).*/\1/p')
    TOTAL_COST=$(echo "$SUMMARY" | sed -n 's/.*"totalCost":\([^,}]*\).*/\1/p')
    UNREALIZED_PL=$(echo "$SUMMARY" | sed -n 's/.*"totalUnrealizedGainLoss":\([^,}]*\).*/\1/p')
    POSITIONS_COUNT=$(echo "$SUMMARY" | sed -n 's/.*"activePositions":\([^,}]*\).*/\1/p')
    
    echo "   💰 Total Market Value: \$$MARKET_VALUE"
    echo "   💸 Total Cost Basis: \$$TOTAL_COST"
    echo "   📈 Unrealized P&L: \$$UNREALIZED_PL"
    echo "   🎯 Active Positions: $POSITIONS_COUNT"
else
    echo -e "${YELLOW}⚠️  Could not retrieve portfolio summary${NC}"
fi

# Show transaction count
TRANSACTION_COUNT=$(curl -s "$BASE_URL/api/transactions/count" | tr -d '"')
echo "   📝 Total Transactions: $TRANSACTION_COUNT"

echo
echo -e "${GREEN}🎉 Sample data population completed!${NC}"
echo
echo "You can now explore the API with realistic data:"
echo "  • View positions: curl $BASE_URL/api/positions/active | jq"
echo "  • View portfolio: curl $BASE_URL/api/portfolio/summary/active | jq"
echo "  • View transactions: curl $BASE_URL/api/transactions | jq" 