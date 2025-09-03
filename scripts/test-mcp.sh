#!/bin/bash

# Portfolio MCP Server Test Script
# Tests the MCP layer functionality

set -e

BASE_URL="${API_BASE_URL:-http://localhost:8081}"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}Portfolio MCP Server Test${NC}"
echo "========================="
echo "Testing MCP at: $BASE_URL"
echo

# Test MCP health
echo "🔍 Testing MCP health..."
if curl -s --connect-timeout 5 "$BASE_URL/mcp/health" >/dev/null 2>&1; then
    echo -e "${GREEN}✅ MCP server is responding${NC}"
else
    echo -e "${RED}❌ MCP server is not accessible${NC}"
    echo "Make sure your Quarkus application is running"
    exit 1
fi

# Get MCP server info
echo "ℹ️  Getting MCP server info..."
SERVER_INFO=$(curl -s "$BASE_URL/mcp/info")
if echo "$SERVER_INFO" | grep -q '"name"'; then
    echo -e "${GREEN}✅ MCP server info retrieved${NC}"
    NAME=$(echo "$SERVER_INFO" | sed -n 's/.*"name":"\([^"]*\)".*/\1/p')
    VERSION=$(echo "$SERVER_INFO" | sed -n 's/.*"version":"\([^"]*\)".*/\1/p')
    TOOL_COUNT=$(echo "$SERVER_INFO" | sed -n 's/.*"toolCount":\([^,}]*\).*/\1/p')
    echo "   Server: $NAME v$VERSION"
    echo "   Tools available: $TOOL_COUNT"
else
    echo -e "${RED}❌ Failed to get MCP server info${NC}"
fi

# List MCP tools
echo "🛠️  Listing MCP tools..."
TOOLS_RESPONSE=$(curl -s "$BASE_URL/mcp/tools")
if echo "$TOOLS_RESPONSE" | grep -q '"tools"'; then
    echo -e "${GREEN}✅ MCP tools listed successfully${NC}"
    TOOL_COUNT=$(echo "$TOOLS_RESPONSE" | sed -n 's/.*"count":\([^,}]*\).*/\1/p')
    echo "   Available tools: $TOOL_COUNT"
else
    echo -e "${RED}❌ Failed to list MCP tools${NC}"
fi

# Test MCP tool call - get portfolio summary
echo "📊 Testing MCP tool call (get_portfolio_summary)..."
MCP_REQUEST='{
    "id": "test-001",
    "method": "tools/call",
    "params": {
        "name": "get_portfolio_summary",
        "arguments": {}
    }
}'

TOOL_RESPONSE=$(curl -s -X POST "$BASE_URL/mcp/tools/call" \
    -H "Content-Type: application/json" \
    -d "$MCP_REQUEST")

if echo "$TOOL_RESPONSE" | grep -q '"result"'; then
    echo -e "${GREEN}✅ MCP tool call successful${NC}"
    
    # Extract some summary data
    if echo "$TOOL_RESPONSE" | grep -q '"totalMarketValue"'; then
        MARKET_VALUE=$(echo "$TOOL_RESPONSE" | sed -n 's/.*"totalMarketValue":\([^,}]*\).*/\1/p')
        TOTAL_COST=$(echo "$TOOL_RESPONSE" | sed -n 's/.*"totalCost":\([^,}]*\).*/\1/p')
        echo "   Portfolio Value: $MARKET_VALUE"
        echo "   Total Cost: $TOTAL_COST"
    fi
else
    echo -e "${RED}❌ MCP tool call failed${NC}"
    echo "Response: $TOOL_RESPONSE"
fi

# Test MCP tool call - get active positions
echo "🎯 Testing MCP tool call (get_active_positions)..."
MCP_REQUEST_POSITIONS='{
    "id": "test-002",
    "method": "tools/call",
    "params": {
        "name": "get_active_positions",
        "arguments": {}
    }
}'

POSITIONS_RESPONSE=$(curl -s -X POST "$BASE_URL/mcp/tools/call" \
    -H "Content-Type: application/json" \
    -d "$MCP_REQUEST_POSITIONS")

if echo "$POSITIONS_RESPONSE" | grep -q '"result"'; then
    echo -e "${GREEN}✅ MCP active positions call successful${NC}"
    
    # Count positions in response
    POSITION_COUNT=$(echo "$POSITIONS_RESPONSE" | grep -o '"ticker"' | wc -l)
    echo "   Active positions found: $POSITION_COUNT"
else
    echo -e "${RED}❌ MCP active positions call failed${NC}"
fi

# Test invalid tool call
echo "❌ Testing invalid MCP tool call..."
INVALID_REQUEST='{
    "id": "test-003",
    "method": "tools/call",
    "params": {
        "name": "nonexistent_tool",
        "arguments": {}
    }
}'

INVALID_RESPONSE=$(curl -s -X POST "$BASE_URL/mcp/tools/call" \
    -H "Content-Type: application/json" \
    -d "$INVALID_REQUEST")

if echo "$INVALID_RESPONSE" | grep -q '"error"\|"success.*false"'; then
    echo -e "${GREEN}✅ Invalid tool call properly rejected${NC}"
else
    echo -e "${YELLOW}⚠️  Invalid tool call handling unexpected${NC}"
fi

echo
echo -e "${GREEN}🎉 MCP server test completed!${NC}"
echo
echo "🚀 To run interactive MCP client:"
echo "   python3 ./start_mcp_server.py --mode interactive"
echo
echo "🧪 To run MCP test mode:"
echo "   python3 ./start_mcp_server.py --mode test" 