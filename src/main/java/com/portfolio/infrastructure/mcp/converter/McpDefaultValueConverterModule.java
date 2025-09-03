package com.portfolio.infrastructure.mcp.converter;

import io.quarkiverse.mcp.server.DefaultValueConverter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MCP DefaultValueConverter module that provides converters for various parameter types.
 * This module centralizes the registration of all default value converters for MCP tools.
 */
@ApplicationScoped
public class McpDefaultValueConverterModule {

    @Produces
    @Singleton
    public DefaultValueConverter<BigDecimal> bigDecimalDefaultValueConverter() {
        return new BigDecimalDefaultValueConverter();
    }

    @Produces
    @Singleton
    public DefaultValueConverter<LocalDate> localDateDefaultValueConverter() {
        return new LocalDateDefaultValueConverter();
    }

    @Produces
    @Singleton
    public DefaultValueConverter<Object> objectDefaultValueConverter() {
        return new ObjectDefaultValueConverter();
    }
}
