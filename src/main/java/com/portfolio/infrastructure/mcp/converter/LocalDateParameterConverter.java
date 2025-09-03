package com.portfolio.infrastructure.mcp.converter;

import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

/**
 * Parameter converter strategy for LocalDate types.
 * Handles conversion of date parameters including transaction dates, start/end dates.
 */
@Singleton
public class LocalDateParameterConverter implements ParameterConverter {

    // TODO: we could do it more extensible reading the parameters from application.properties
    private static final Set<String> SUPPORTED_PARAMETERS = Set.of(
            "date", "startDate", "endDate", "transactionDate", 
            "createdDate", "updatedDate", "fromDate", "toDate"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String TODAY_KEYWORD = "TODAY";

    @Override
    public Object convert(Object value, String parameterName) {
        switch (value) {
            case null -> {
                return null;
            }
            case LocalDate localDate -> {
                return localDate;
            }
            case String str -> {
                if (str.trim().isEmpty()) {
                    return null;
                }

                String trimmedStr = str.trim();
                if (TODAY_KEYWORD.equalsIgnoreCase(trimmedStr)) {
                    return LocalDate.now();
                }

                try {
                    return LocalDate.parse(trimmedStr, DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    throw new ToolCallException("Invalid LocalDate value for parameter '" + parameterName +
                            "': " + str + ". Expected format: YYYY-MM-DD or 'TODAY'");
                }
            }
            default -> {
            }
        }

        throw new ToolCallException("Cannot convert " + value.getClass().getSimpleName() +
                " to LocalDate for parameter '" + parameterName + "'");
    }

    @Override
    public boolean matches(String parameterName) {
        return SUPPORTED_PARAMETERS.contains(parameterName);
    }

    @Override
    public Class<?> getTargetType() {
        return LocalDate.class;
    }
}
