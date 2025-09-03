#!/bin/bash

# Portfolio Management API Testing Script
# This script tests all REST API endpoints for the portfolio management system

set -e  # Exit on any error

# Configuration
BASE_URL="${API_BASE_URL:-http://localhost:8080}"
VERBOSE="${VERBOSE:-false}"
CLEANUP="${CLEANUP:-true}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Global variables to store created IDs
TRANSACTION_IDS=()
TEST_DATA_FILE="/tmp/portfolio_test_data.json"

# Utility functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_section() {
    echo
    echo -e "${PURPLE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${PURPLE} $1${NC}"
    echo -e "${PURPLE}═══════════════════════════════════════════════════════════════${NC}"
    echo
}

# Function to make API calls with error handling
api_call() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local description="$4"
    
    log_info "Testing: $description"
    
    if [ "$VERBOSE" = "true" ]; then
        echo -e "${CYAN}Request:${NC} $method $endpoint"
        if [ -n "$data" ]; then
            echo -e "${CYAN}Data:${NC} $data"
        fi
    fi
    
    local response
    local http_code
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
            -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
            -X "$method" \
            "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo "$response" | sed -e 's/HTTPSTATUS\:.*//g')
    
    if [[ "$http_code" -ge 200 && "$http_code" -lt 300 ]]; then
        log_success "✅ $description (HTTP $http_code)"
        if [ "$VERBOSE" = "true" ] && [ -n "$body" ]; then
            echo "$body" | jq '.' 2>/dev/null || echo "$body"
        fi
        echo "$body"
        return 0
    else
        log_error "❌ $description (HTTP $http_code)"
        if [ -n "$body" ]; then
            echo -e "${RED}Response:${NC} $body"
        fi
        return 1
    fi
}

# Function to extract ID from JSON response
extract_id() {
    echo "$1" | jq -r '.id // empty' 2>/dev/null
}

# Function to check if API is running
check_api_health() {
    log_section "API Health Check"
    
    log_info "Checking if API is accessible at $BASE_URL"
    
    if curl -s --connect-timeout 5 "$BASE_URL/q/health" >/dev/null 2>&1; then
        log_success "API is accessible"
    elif curl -s --connect-timeout 5 "$BASE_URL/api/transactions/count" >/dev/null 2>&1; then
        log_success "API is accessible (health endpoint not available)"
    else
        log_error "API is not accessible at $BASE_URL"
        log_error "Please ensure the application is running"
        exit 1
    fi
}

# Test transaction management
test_transactions() {
    log_section "Transaction Management Tests"
    
    # Create transactions
    log_info "Creating test transactions..."
    
    # Create AAPL transaction
    local aapl_response
    aapl_response=$(api_call "POST" "/api/transactions" '{
        "ticker": "AAPL",
        "transactionType": "BUY",
        "quantity": 10.0,
        "price": 150.00,
        "fees": 5.00,
        "currency": "USD",
        "transactionDate": "2024-01-15",
        "notes": "Initial Apple purchase - Test"
    }' "Create AAPL BUY transaction")
    
    local aapl_id
    aapl_id=$(extract_id "$aapl_response")
    if [ -n "$aapl_id" ]; then
        TRANSACTION_IDS+=("$aapl_id")
        log_success "Created AAPL transaction with ID: $aapl_id"
    fi
    
    # Create MSFT transaction
    local msft_response
    msft_response=$(api_call "POST" "/api/transactions" '{
        "ticker": "MSFT",
        "transactionType": "BUY",
        "quantity": 5.0,
        "price": 380.00,
        "fees": 7.50,
        "currency": "USD",
        "transactionDate": "2024-01-20",
        "notes": "Microsoft investment - Test"
    }' "Create MSFT BUY transaction")
    
    local msft_id
    msft_id=$(extract_id "$msft_response")
    if [ -n "$msft_id" ]; then
        TRANSACTION_IDS+=("$msft_id")
        log_success "Created MSFT transaction with ID: $msft_id"
    fi
    
    # Create additional AAPL transaction
    local aapl2_response
    aapl2_response=$(api_call "POST" "/api/transactions" '{
        "ticker": "AAPL",
        "transactionType": "BUY",
        "quantity": 5.0,
        "price": 155.00,
        "fees": 3.00,
        "currency": "USD",
        "transactionDate": "2024-02-01",
        "notes": "Additional Apple shares - Test"
    }' "Create second AAPL BUY transaction")
    
    local aapl2_id
    aapl2_id=$(extract_id "$aapl2_response")
    if [ -n "$aapl2_id" ]; then
        TRANSACTION_IDS+=("$aapl2_id")
        log_success "Created second AAPL transaction with ID: $aapl2_id"
    fi
    
    # Test GET operations
    api_call "GET" "/api/transactions" "" "Get all transactions" >/dev/null
    api_call "GET" "/api/transactions/ticker/AAPL" "" "Get AAPL transactions" >/dev/null
    api_call "GET" "/api/transactions/count" "" "Get transaction count" >/dev/null
    api_call "GET" "/api/transactions/count/AAPL" "" "Get AAPL transaction count" >/dev/null
    
    # Test search
    api_call "GET" "/api/transactions/search?ticker=AAPL&type=BUY" "" "Search AAPL BUY transactions" >/dev/null
    api_call "GET" "/api/transactions/search?fromDate=2024-01-01&toDate=2024-12-31" "" "Search transactions by date range" >/dev/null
    
    # Test individual transaction retrieval
    if [ -n "$aapl_id" ]; then
        api_call "GET" "/api/transactions/$aapl_id" "" "Get transaction by ID" >/dev/null
    fi
    
    # Test transaction update
    if [ -n "$aapl_id" ]; then
        api_call "PUT" "/api/transactions/$aapl_id" '{
            "ticker": "AAPL",
            "transactionType": "BUY",
            "quantity": 12.0,
            "price": 148.00,
            "fees": 4.00,
            "currency": "USD",
            "transactionDate": "2024-01-15",
            "notes": "Updated initial purchase - Test"
        }' "Update AAPL transaction" >/dev/null
    fi
    
    # Store transaction IDs for cleanup
    echo "${TRANSACTION_IDS[@]}" > "$TEST_DATA_FILE"
}

# Test position management
test_positions() {
    log_section "Position Management Tests"
    
    # Wait a moment for positions to be calculated
    sleep 1
    
    # Test position retrieval
    api_call "GET" "/api/positions" "" "Get all positions" >/dev/null
    api_call "GET" "/api/positions/active" "" "Get active positions" >/dev/null
    api_call "GET" "/api/positions/count" "" "Get position count" >/dev/null
    api_call "GET" "/api/positions/count/active" "" "Get active position count" >/dev/null
    
    # Test position by ticker
    api_call "GET" "/api/positions/ticker/AAPL" "" "Get AAPL position" >/dev/null
    api_call "GET" "/api/positions/ticker/MSFT" "" "Get MSFT position" >/dev/null
    
    # Test position existence check
    api_call "GET" "/api/positions/ticker/AAPL/exists" "" "Check if AAPL position exists" >/dev/null
    api_call "GET" "/api/positions/ticker/NONEXISTENT/exists" "" "Check if non-existent position exists" >/dev/null
    
    # Test market price updates
    api_call "PUT" "/api/positions/ticker/AAPL/price" '{
        "price": 175.50
    }' "Update AAPL market price" >/dev/null
    
    api_call "PUT" "/api/positions/ticker/MSFT/price" '{
        "price": 420.75
    }' "Update MSFT market price" >/dev/null
    
    # Test position recalculation
    api_call "POST" "/api/positions/ticker/AAPL/recalculate" "" "Recalculate AAPL position" >/dev/null
    api_call "POST" "/api/positions/ticker/MSFT/recalculate" "" "Recalculate MSFT position" >/dev/null
}

# Test portfolio summary
test_portfolio() {
    log_section "Portfolio Summary Tests"
    
    # Wait a moment for calculations to complete
    sleep 1
    
    api_call "GET" "/api/portfolio/summary" "" "Get complete portfolio summary" >/dev/null
    api_call "GET" "/api/portfolio/summary/active" "" "Get active portfolio summary" >/dev/null
}

# Test error scenarios
test_error_scenarios() {
    log_section "Error Scenario Tests"
    
    log_info "Testing error handling..."
    
    # Test invalid transaction data
    if ! api_call "POST" "/api/transactions" '{
        "ticker": "INVALID",
        "transactionType": "BUY",
        "quantity": -10.0,
        "price": -50.00,
        "currency": "USD",
        "transactionDate": "2024-01-15"
    }' "Create invalid transaction (should fail)" 2>/dev/null; then
        log_success "✅ Invalid transaction properly rejected"
    else
        log_warning "⚠️  Invalid transaction was accepted (unexpected)"
    fi
    
    # Test non-existent transaction
    if ! api_call "GET" "/api/transactions/00000000-0000-0000-0000-000000000000" "" "Get non-existent transaction (should fail)" 2>/dev/null; then
        log_success "✅ Non-existent transaction properly handled"
    else
        log_warning "⚠️  Non-existent transaction returned success (unexpected)"
    fi
    
    # Test invalid market price update
    if ! api_call "PUT" "/api/positions/ticker/NONEXISTENT/price" '{
        "price": 100.00
    }' "Update price for non-existent position (should fail)" 2>/dev/null; then
        log_success "✅ Invalid price update properly rejected"
    else
        log_warning "⚠️  Invalid price update was accepted (unexpected)"
    fi
}

# Cleanup function
cleanup_test_data() {
    if [ "$CLEANUP" = "true" ]; then
        log_section "Cleanup"
        
        log_info "Cleaning up test data..."
        
        # Read transaction IDs from file if it exists
        if [ -f "$TEST_DATA_FILE" ]; then
            mapfile -t STORED_IDS < "$TEST_DATA_FILE"
            TRANSACTION_IDS+=("${STORED_IDS[@]}")
        fi
        
        local deleted_count=0
        for transaction_id in "${TRANSACTION_IDS[@]}"; do
            if [ -n "$transaction_id" ] && [ "$transaction_id" != "null" ]; then
                if api_call "DELETE" "/api/transactions/$transaction_id" "" "Delete transaction $transaction_id" 2>/dev/null; then
                    ((deleted_count++))
                fi
            fi
        done
        
        log_success "Deleted $deleted_count test transactions"
        
        # Clean up temp files
        rm -f "$TEST_DATA_FILE"
    else
        log_warning "Cleanup disabled - test data remains in system"
    fi
}

# Show test results summary
show_summary() {
    log_section "Test Summary"
    
    # Get final counts
    local transaction_count
    transaction_count=$(api_call "GET" "/api/transactions/count" "" "Final transaction count" | tr -d '"' || echo "0")
    
    local position_count
    position_count=$(api_call "GET" "/api/positions/count/active" "" "Final active position count" | tr -d '"' || echo "0")
    
    log_info "Final system state:"
    echo "  📊 Total transactions: $transaction_count"
    echo "  🎯 Active positions: $position_count"
    
    if [ "$CLEANUP" = "true" ]; then
        log_success "✅ All tests completed successfully with cleanup"
    else
        log_success "✅ All tests completed successfully (test data preserved)"
    fi
}

# Display usage information
usage() {
    echo "Portfolio Management API Test Script"
    echo
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  -h, --help      Show this help message"
    echo "  -v, --verbose   Enable verbose output"
    echo "  --no-cleanup    Skip cleanup of test data"
    echo "  --base-url URL  Set API base URL (default: http://localhost:8080)"
    echo
    echo "Environment Variables:"
    echo "  API_BASE_URL    Set API base URL"
    echo "  VERBOSE         Enable verbose output (true/false)"
    echo "  CLEANUP         Enable cleanup (true/false)"
    echo
    echo "Examples:"
    echo "  $0                           # Run tests with default settings"
    echo "  $0 --verbose                 # Run tests with verbose output"
    echo "  $0 --no-cleanup              # Run tests without cleanup"
    echo "  $0 --base-url http://api:8080 # Run tests against different URL"
}

# Parse command line arguments
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                usage
                exit 0
                ;;
            -v|--verbose)
                VERBOSE="true"
                shift
                ;;
            --no-cleanup)
                CLEANUP="false"
                shift
                ;;
            --base-url)
                BASE_URL="$2"
                shift 2
                ;;
            *)
                log_error "Unknown option: $1"
                usage
                exit 1
                ;;
        esac
    done
}

# Main execution
main() {
    parse_args "$@"
    
    # Display configuration
    echo -e "${CYAN}Portfolio Management API Test Suite${NC}"
    echo -e "${CYAN}====================================${NC}"
    echo "Base URL: $BASE_URL"
    echo "Verbose: $VERBOSE"
    echo "Cleanup: $CLEANUP"
    echo
    
    # Set up cleanup trap
    trap 'cleanup_test_data; exit 1' INT TERM ERR
    
    # Check dependencies
    if ! command -v curl &> /dev/null; then
        log_error "curl is required but not installed"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        log_warning "jq is not installed - JSON output will not be formatted"
    fi
    
    # Run tests
    check_api_health
    test_transactions
    test_positions
    test_portfolio
    test_error_scenarios
    
    # Cleanup and summary
    cleanup_test_data
    show_summary
    
    log_success "🎉 All API tests completed successfully!"
}

# Run main function with all arguments
main "$@" 