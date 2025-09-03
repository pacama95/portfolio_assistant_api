package com.portfolio.infrastructure.mcp.converter;

import io.quarkiverse.mcp.server.DefaultValueConverter;
import jakarta.inject.Singleton;

import java.math.BigDecimal;

/**
 * Default value converter for BigDecimal parameters in MCP tools.
 * Converts string default values to BigDecimal objects.
 */
@Singleton
public class BigDecimalDefaultValueConverter implements DefaultValueConverter<BigDecimal> {

    @Override
    public BigDecimal convert(String defaultValue) {
        if (defaultValue == null || defaultValue.trim().isEmpty()) {
            return null;
        }
        
        try {
            return new BigDecimal(defaultValue.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid default value for BigDecimal: " + defaultValue, e);
        }
    }
}
