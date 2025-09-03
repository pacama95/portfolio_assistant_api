package com.portfolio.infrastructure.mcp.converter;

import io.quarkiverse.mcp.server.DefaultValueConverter;
import jakarta.inject.Singleton;

/**
 * Default value converter for Object parameters in MCP tools.
 * This converter simply returns the string value as-is, since our runtime
 * ParameterConversionService handles the actual type conversion.
 */
@Singleton
public class ObjectDefaultValueConverter implements DefaultValueConverter<Object> {

    @Override
    public Object convert(String defaultValue) {
        // Simply return the string value as-is
        // The runtime ParameterConversionService will handle the actual conversion
        return defaultValue;
    }
}
