package com.portfolio;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Quarkus application entry point
 */
@QuarkusMain
@OpenAPIDefinition(
    info = @Info(
        title = "Portfolio Management API",
        version = "1.0.0",
        description = "Reactive portfolio management system with CRUD operations for transactions, positions, and market data",
        contact = @Contact(
            name = "Portfolio Support",
            email = "support@your_portfolio.com"
        ),
        license = @License(
            name = "MIT",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "Development server")
    }
)
public class PortfolioApplication implements QuarkusApplication {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioApplication.class);

    public static void main(String... args) {
        Quarkus.run(PortfolioApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        logger.info("Starting Portfolio Management Application...");
        
        // Debug: Log environment variables (remove after debugging)
        String twelveDataKey = System.getenv("TWELVE_DATA_API_KEY");
        logger.info("TWELVE_DATA_API_KEY environment variable: {}", 
                   twelveDataKey != null ? "SET (length: " + twelveDataKey.length() + ")" : "NOT SET");
        
        // Log all database-related environment variables
        logger.info("=== Database Environment Variables ===");
        logger.info("PGHOST: {}", System.getenv("PGHOST"));
        logger.info("PGPORT: {}", System.getenv("PGPORT"));  
        logger.info("PGDATABASE: {}", System.getenv("PGDATABASE"));
        logger.info("PGUSER: {}", System.getenv("PGUSER"));
        logger.info("PGPASSWORD present: {}", System.getenv("PGPASSWORD") != null);
        
        // Railway might use different variable names
        logger.info("DATABASE_URL present: {}", System.getenv("DATABASE_URL") != null);
        logger.info("POSTGRES_HOST: {}", System.getenv("POSTGRES_HOST"));
        logger.info("POSTGRES_PORT: {}", System.getenv("POSTGRES_PORT"));
        logger.info("POSTGRES_DB: {}", System.getenv("POSTGRES_DB"));
        logger.info("POSTGRES_USER: {}", System.getenv("POSTGRES_USER"));
        logger.info("POSTGRES_PASSWORD present: {}", System.getenv("POSTGRES_PASSWORD") != null);
        
        logger.info("PORT: {}", System.getenv("PORT"));
        
        logger.info("Application is ready to accept connections");
        
        Quarkus.waitForExit();
        return 0;
    }
} 