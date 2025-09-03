package com.portfolio.infrastructure.mcp.converter;

/**
 * Strategy interface for converting Object parameters to specific types based on parameter names.
 * Each implementation handles conversion for specific parameter names and target types.
 */
public interface ParameterConverter {

    /**
     * Converts the input value to the appropriate target type.
     * 
     * @param value The input value to convert
     * @param parameterName The name of the parameter being converted (for error messages)
     * @return The converted value
     * @throws io.quarkiverse.mcp.server.ToolCallException if conversion fails
     */
    Object convert(Object value, String parameterName);

    /**
     * Determines if this converter can handle the given parameter name.
     * 
     * @param parameterName The name of the parameter to check
     * @return true if this converter can handle the parameter, false otherwise
     */
    boolean matches(String parameterName);

    /**
     * Returns the target type that this converter produces.
     * Used for documentation and debugging purposes.
     * 
     * @return The target class this converter produces
     */
    Class<?> getTargetType();
}
