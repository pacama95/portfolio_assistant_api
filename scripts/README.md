# Portfolio Management API Test Scripts

This directory contains various scripts for testing and interacting with the Portfolio Management API.

## Scripts Overview

### 🧪 `test-api.sh` - Comprehensive API Testing
**Main testing script that validates all API endpoints**

```bash
# Run with default settings
./scripts/test-api.sh

# Run with verbose output
./scripts/test-api.sh --verbose

# Run without cleanup (preserve test data)
./scripts/test-api.sh --no-cleanup

# Run against different URL
./scripts/test-api.sh --base-url http://api.example.com:8080
```

**Features:**
- ✅ Tests all REST endpoints (transactions, positions, portfolio)
- ✅ Validates error handling and edge cases
- ✅ Automatic test data cleanup
- ✅ Colored output with detailed logging
- ✅ Comprehensive error reporting

### ⚡ `quick-test.sh` - Basic Functionality Test
**Quick verification that the API is working**

```bash
./scripts/quick-test.sh
```

**What it does:**
- Checks API health
- Creates a test transaction
- Verifies position calculation
- Updates market price
- Gets portfolio summary
- Cleans up test data

### 📊 `sample-data.sh` - Sample Data Population
**Populates the system with realistic sample data**

```bash
./scripts/sample-data.sh
```

**Creates:**
- Multiple transactions for major stocks (AAPL, MSFT, GOOGL, TSLA, AMZN)
- Different transaction types (BUY, SELL, DIVIDEND)
- Realistic price updates
- Portfolio with gains/losses

## Environment Variables

All scripts support these environment variables:

```bash
export API_BASE_URL="http://localhost:8080"  # API base URL
export VERBOSE="true"                        # Enable verbose output
export CLEANUP="false"                       # Disable test data cleanup
```

## Prerequisites

### Required Tools
- `curl` - For making HTTP requests
- `bash` - For running the scripts

### Optional Tools
- `jq` - For pretty JSON formatting (recommended)

### Installation on macOS
```bash
brew install curl jq
```

### Installation on Ubuntu/Debian
```bash
sudo apt-get install curl jq
```

## Usage Examples

### 1. Quick API Verification
```bash
# Make sure your API is running, then:
./scripts/quick-test.sh
```

### 2. Full API Testing
```bash
# Comprehensive test with cleanup
./scripts/test-api.sh

# Verbose testing for debugging
./scripts/test-api.sh --verbose

# Test and keep data for inspection
./scripts/test-api.sh --no-cleanup
```

### 3. Development Setup
```bash
# Populate with sample data for development
./scripts/sample-data.sh

# Then run your development work...

# Clean up when done
./scripts/test-api.sh --no-cleanup  # Creates some test data
# Or manually delete through API
```

### 4. CI/CD Pipeline
```bash
# In your CI pipeline
export API_BASE_URL="http://test-api:8080"
./scripts/test-api.sh --verbose
```

## Script Output

### Success Example
```
Portfolio Management API Test Suite
====================================
Base URL: http://localhost:8080
Verbose: false
Cleanup: true

═══════════════════════════════════════════════════════════════
 API Health Check
═══════════════════════════════════════════════════════════════

[INFO] Checking if API is accessible at http://localhost:8080
[SUCCESS] API is accessible

[... more tests ...]

[SUCCESS] 🎉 All API tests completed successfully!
```

### Error Example
```
[ERROR] ❌ Create AAPL BUY transaction (HTTP 400)
Response: {"error": "Validation failed: quantity must be positive"}
```

## Troubleshooting

### API Not Accessible
```
[ERROR] API is not accessible at http://localhost:8080
[ERROR] Please ensure the application is running
```

**Solution:** Start your Quarkus application:
```bash
./gradlew quarkusDev
# or
java -jar build/quarkus-app/quarkus-run.jar
```

### Permission Denied
```
bash: ./scripts/test-api.sh: Permission denied
```

**Solution:** Make scripts executable:
```bash
chmod +x scripts/*.sh
```

### JSON Parsing Issues
If you see warnings about JSON formatting:
```bash
# Install jq for better JSON handling
brew install jq  # macOS
# or
sudo apt-get install jq  # Ubuntu/Debian
```

## Custom Testing

You can also create custom test scenarios by combining the scripts:

```bash
# 1. Populate with sample data
./scripts/sample-data.sh

# 2. Run your custom tests
curl "$API_BASE_URL/api/positions/active" | jq

# 3. Clean up
./scripts/test-api.sh --no-cleanup  # Will clean existing data
```

## Script Development

To create your own test scripts, follow these patterns:

```bash
#!/bin/bash
set -e  # Exit on error

BASE_URL="${API_BASE_URL:-http://localhost:8080}"

# Test API call
response=$(curl -s -X GET "$BASE_URL/api/transactions/count")
echo "Transaction count: $response"
```

## Integration with Development Workflow

### During Development
1. `./scripts/sample-data.sh` - Populate test data
2. Make your changes
3. `./scripts/quick-test.sh` - Verify basic functionality
4. `./scripts/test-api.sh` - Full regression testing

### Before Deployment
1. `./scripts/test-api.sh --verbose` - Comprehensive testing
2. Review logs for any issues
3. Deploy with confidence

---

For more information about the API endpoints, see the main project documentation. 