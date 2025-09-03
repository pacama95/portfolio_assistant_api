# Portfolio Assistant API

A comprehensive portfolio assistant API built with **Quarkus**, featuring reactive programming, clean architecture, and multiple interfaces (REST API & MCP).

## 🏗️ Architecture Overview

This system follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Interface Layer                          │
│  ┌─────────────────┐           ┌─────────────────┐         │
│  │   REST API      │           │   MCP Server    │         │
│  │   Controllers   │           │   Tools         │         │
│  └─────────────────┘           └─────────────────┘         │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                  Application Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │  Transaction    │  │    Position     │  │  Portfolio   │ │
│  │   Use Cases     │  │   Use Cases     │  │  Use Cases   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Transaction   │  │    Position     │  │   Currency   │ │
│  │     Model       │  │     Model       │  │    Enums     │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                Infrastructure Layer                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   PostgreSQL    │  │     Panache     │  │     JSON     │ │
│  │   Database      │  │   Repositories  │  │   Mappers    │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Features

### 💼 **Portfolio Management**
- ✅ **Transaction Management**: Create, read, update, delete transactions
- ✅ **Position Tracking**: Automatic position calculation from transactions
- ✅ **Market Data**: Real-time price updates and P&L calculations
- ✅ **Portfolio Summary**: Comprehensive portfolio metrics and analytics
- ✅ **Multi-Currency Support**: USD, EUR, GBP with proper handling

### 🔄 **Reactive Architecture**
- ✅ **Non-blocking I/O**: Built with Quarkus reactive stack
- ✅ **Async Operations**: All database operations are reactive
- ✅ **Performance**: High throughput with efficient resource usage
- ✅ **Scalability**: Designed for cloud-native deployments

### 🌐 **Multiple Interfaces**
- ✅ **REST API**: Complete HTTP API for web/mobile applications
- ✅ **MCP Server**: Model Context Protocol for AI integrations
- ✅ **Clean Separation**: Both interfaces use same business logic

### 🗄️ **Data Management**
- ✅ **PostgreSQL**: Production-ready database with custom types
- ✅ **Schema Management**: Comprehensive database schema with triggers
- ✅ **Automatic Calculations**: Position recalculation via database triggers
- ✅ **Data Integrity**: Proper constraints and validation

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Framework** | Quarkus (Reactive) |
| **Language** | Java 21 |
| **Database** | PostgreSQL 14+ |
| **ORM** | Hibernate Reactive with Panache |
| **API** | JAX-RS (REST) + MCP Protocol |
| **Build** | Gradle |
| **Testing** | JUnit 5 + TestContainers |

## 📦 Project Structure

```
src/main/java/com/portfolio/
├── application/usecase/          # Business Use Cases
│   ├── transaction/              # Transaction operations
│   ├── position/                 # Position operations
│   └── portfolio/                # Portfolio operations
├── domain/                       # Domain Models & Logic
│   ├── model/                    # Core entities
│   ├── port/                     # Repository interfaces
│   └── valueobject/              # Value objects
└── infrastructure/               # External Interfaces
    ├── persistence/              # Database implementation
    │   ├── entity/               # JPA entities
    │   ├── repository/           # Panache repositories
    │   └── adapter/              # Repository adapters
    ├── rest/                     # REST API
    │   ├── dto/                  # Data transfer objects
    │   └── mapper/               # Domain ↔ DTO mapping
    └── mcp/                      # MCP Server
        ├── dto/                  # MCP protocol DTOs
        ├── tools/                # MCP tool registry
        └── config/               # MCP configuration
```

## 🚀 Quick Start

### Prerequisites
- **Java 21+**
- **PostgreSQL 14+**
- **Docker** (optional, for database)

### 1. **Database Setup**
```bash
# Option A: Using Docker
docker-compose up -d

# Option B: Local PostgreSQL
createdb portfolio_db
psql portfolio_db < src/main/resources/db/migration/schema.sql
```

### 2. **Application Configuration**
```properties
# application.properties
quarkus.datasource.reactive.url=postgresql://localhost:5432/portfolio_db
quarkus.datasource.username=portfolio_user
quarkus.datasource.password=your_password
```

### 3. **Run the Application**
```bash
# Development mode (with live reload)
./gradlew quarkusDev

# Production mode
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

### 4. **Verify Installation**
```bash
# Test REST API
./scripts/quick-test.sh

# Test MCP Server
./scripts/test-mcp.sh
```

## 📡 API Documentation

### 🌐 **REST API Endpoints**

#### **Transactions** (`/api/transactions`)
```bash
POST   /api/transactions              # Create transaction
GET    /api/transactions              # List all transactions
GET    /api/transactions/{id}         # Get transaction by ID
PUT    /api/transactions/{id}         # Update transaction
DELETE /api/transactions/{id}         # Delete transaction
GET    /api/transactions/ticker/{ticker}    # Get by ticker
GET    /api/transactions/search       # Search with filters
```

#### **Positions** (`/api/positions`)
```bash
GET    /api/positions                 # List all positions
GET    /api/positions/active          # List active positions
GET    /api/positions/{id}            # Get position by ID
GET    /api/positions/ticker/{ticker} # Get position by ticker
PUT    /api/positions/ticker/{ticker}/price # Update market price
POST   /api/positions/ticker/{ticker}/recalculate # Recalculate position
```

#### **Portfolio** (`/api/portfolio`)
```bash
GET    /api/portfolio/summary         # Complete portfolio summary
GET    /api/portfolio/summary/active  # Active portfolio summary
```

### 🤖 **MCP Server Tools**

#### **Transaction Tools**
- `create_transaction` - Create a new transaction
- `get_transaction` - Get transaction by ID  
- `update_transaction` - Update existing transaction
- `delete_transaction` - Delete transaction
- `get_transactions_by_ticker` - Get transactions for ticker
- `search_transactions` - Search with filters

#### **Position Tools**
- `get_all_positions` - Get all positions
- `get_active_positions` - Get active positions
- `get_position_by_ticker` - Get position by ticker
- `update_market_price` - Update market price
- `recalculate_position` - Recalculate position

#### **Portfolio Tools**
- `get_portfolio_summary` - Complete portfolio summary
- `get_active_portfolio_summary` - Active portfolio summary

## 🧪 Testing

### **API Testing**
```bash
# Comprehensive API testing
./scripts/test-api.sh

# Quick functionality test
./scripts/quick-test.sh

# Populate sample data
./scripts/sample-data.sh
```

### **MCP Testing**
```bash
# Test MCP server
./scripts/test-mcp.sh

# Interactive MCP client
python3 ./start_mcp_server.py --mode interactive

# Automated MCP testing
python3 ./start_mcp_server.py --mode test
```

### **Development Testing**
```bash
# Run unit tests
./gradlew test

# Run with test coverage
./gradlew test jacocoTestReport

# Integration testing
./gradlew integrationTest
```

## 📊 Example Usage

### **REST API Examples**

```bash
# Create a transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "transactionType": "BUY",
    "quantity": 10,
    "price": 150.00,
    "currency": "USD",
    "transactionDate": "2024-01-15"
  }'

# Get portfolio summary
curl http://localhost:8080/api/portfolio/summary/active

# Update market price
curl -X PUT http://localhost:8080/api/positions/ticker/AAPL/price \
  -H "Content-Type: application/json" \
  -d '{"price": 175.50}'
```

### **MCP Examples**

```python
# Using the Python MCP client
async with PortfolioMcpClient() as client:
    # Create transaction
    result = await client.call_tool('create_transaction', {
        "ticker": "AAPL",
        "transactionType": "BUY",
        "quantity": 10,
        "price": 150.00,
        "currency": "USD",
        "transactionDate": "2024-01-15"
    })
    
    # Get portfolio summary
    summary = await client.call_tool('get_portfolio_summary', {})
```

## 🔧 Configuration

### **Database Configuration**
```properties
# PostgreSQL Configuration
quarkus.datasource.db-kind=postgresql
quarkus.datasource.reactive.url=postgresql://localhost:5432/portfolio_db
quarkus.datasource.username=portfolio_user
quarkus.datasource.password=secure_password

# Hibernate Configuration
quarkus.hibernate-orm.database.generation=validate
quarkus.hibernate-orm.sql-load-script=no-file
```

### **Server Configuration**
```properties
# HTTP Server
quarkus.http.port=8080
quarkus.http.host=0.0.0.0

# Development
quarkus.live-reload.instrumentation=true
quarkus.dev.console.enable=true
```

## 🚀 Deployment

### **Docker Deployment**
```bash
# Build Docker image
./gradlew build -Dquarkus.package.type=native-sources
docker build -f src/main/docker/Dockerfile.native -t portfolio-management .

# Run with docker-compose
docker-compose up -d
```

### **Kubernetes Deployment**
```bash
# Generate Kubernetes manifests
./gradlew build -Dquarkus.kubernetes.deploy=true

# Apply manifests
kubectl apply -f target/kubernetes/
```

## 🤝 Development

### **Adding New Features**
1. **Domain Layer**: Add new entities/value objects
2. **Application Layer**: Create use cases
3. **Infrastructure Layer**: Implement adapters
4. **Interface Layer**: Add REST endpoints and/or MCP tools

### **Code Style**
- Follow Java naming conventions
- Use reactive patterns (Uni/Multi)
- Maintain clean architecture boundaries
- Write comprehensive tests

## 📚 Documentation

- **API Documentation**: Available at `/q/swagger-ui` when running
- **Health Checks**: Available at `/q/health`
- **Metrics**: Available at `/q/metrics`
- **MCP Tools**: Available at `/mcp/tools`

## 🛡️ Security Features

- ✅ **Input Validation**: Bean validation on all DTOs
- ✅ **SQL Injection Protection**: Parameterized queries
- ✅ **CORS Configuration**: Configurable cross-origin policies
- ✅ **Error Handling**: Consistent error responses

## 🔮 Roadmap

- [ ] **Authentication & Authorization**
- [ ] **WebSocket Support** for real-time updates
- [ ] **GraphQL API** as additional interface
- [ ] **Event Sourcing** for audit trails
- [ ] **Caching Layer** with Redis
- [ ] **Microservices Split** (optional)

---

**Built with ❤️ using Quarkus and Clean Architecture principles** 