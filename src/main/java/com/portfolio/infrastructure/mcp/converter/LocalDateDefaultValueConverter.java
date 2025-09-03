package com.portfolio.infrastructure.mcp.converter;

import io.quarkiverse.mcp.server.DefaultValueConverter;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Default value converter for LocalDate parameters in MCP tools.
 * Converts string default values to LocalDate objects.
 * Supports the following formats:
 * - ISO local date format (YYYY-MM-DD)
 * - Special value "TODAY" for current date
 */
@Singleton
public class LocalDateDefaultValueConverter implements DefaultValueConverter<LocalDate> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String TODAY_KEYWORD = "TODAY";

    @Override
    public LocalDate convert(String defaultValue) {
        if (defaultValue == null || defaultValue.trim().isEmpty()) {
            return null;
        }
        
        String trimmedValue = defaultValue.trim();
        
        // Handle special keyword for today's date
        if (TODAY_KEYWORD.equalsIgnoreCase(trimmedValue)) {
            return LocalDate.now();
        }
        
        try {
            return LocalDate.parse(trimmedValue, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid default value for LocalDate: " + defaultValue + 
                    ". Expected format: YYYY-MM-DD or 'TODAY'", e);
        }
    }
}
