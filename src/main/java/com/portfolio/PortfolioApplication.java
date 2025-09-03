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
        
        Quarkus.waitForExit();
        return 0;
    }
} 